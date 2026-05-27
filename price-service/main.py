"""
price-service: a thin FastAPI wrapper around SupermarktConnector.

Exposes a normalised /search endpoint that the Java backend calls over the
Docker network. Talks to the (unofficial) Albert Heijn and Jumbo mobile APIs,
so live results depend on those endpoints being reachable from this container.
"""
import json
import logging
import os
from concurrent.futures import ThreadPoolExecutor, TimeoutError as FutureTimeout
from typing import Any, List, Optional

from fastapi import FastAPI, Query
from pydantic import BaseModel

LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO").upper()
logging.basicConfig(level=LOG_LEVEL, format="%(asctime)s %(levelname)s %(name)s | %(message)s")
logger = logging.getLogger("price-service")

# Log the real HTTP exchange with the supermarket APIs: urllib3 emits the
# request line and response status (e.g. the Jumbo v15 404) at DEBUG level.
logging.getLogger("urllib3").setLevel(logging.DEBUG)


def _truncate(obj: Any, limit: int = 1500) -> str:
    """Compact JSON for logging, truncated so replies don't flood the logs."""
    text = json.dumps(obj, ensure_ascii=False, default=str)
    return text if len(text) <= limit else f"{text[:limit]}... [{len(text)} chars total]"

# Per-chain deadline. The unofficial APIs can hang (e.g. Jumbo's endpoint),
# so we abandon a slow chain rather than let it block the whole request.
CHAIN_TIMEOUT_S = 6

app = FastAPI(title="shophelp price-service", version="0.1.0")


class PriceResult(BaseModel):
    chain: str
    name: Optional[str] = None
    unitSize: Optional[str] = None
    price: Optional[float] = None
    currency: str = "EUR"


class SearchResponse(BaseModel):
    query: str
    count: int
    results: List[PriceResult]
    errors: List[str] = []


@app.get("/health")
def health():
    return {"status": "ok"}


def _search_ah(query: str, size: int) -> List[PriceResult]:
    from supermarktconnector.ah import AHConnector

    logger.info("AH request  -> search_products(query=%r, size=%d)", query, size)
    connector = AHConnector()
    data = connector.search_products(query=query, size=size, page=0)
    products = data.get("products", [])
    logger.info("AH reply    <- %d product(s); raw=%s", len(products), _truncate(data))
    results = []
    for p in products:
        results.append(PriceResult(
            chain="Albert Heijn",
            name=p.get("title"),
            unitSize=p.get("salesUnitSize"),
            price=p.get("priceBeforeBonus"),
        ))
    return results


def _search_jumbo(query: str, size: int) -> List[PriceResult]:
    from supermarktconnector.jumbo import JumboConnector

    logger.info("Jumbo request  -> search_products(query=%r, size=%d)", query, size)
    connector = JumboConnector()
    data = connector.search_products(query=query, size=size, page=0)
    products = data.get("products", {}).get("data", [])
    logger.info("Jumbo reply    <- %d product(s); raw=%s", len(products), _truncate(data))
    results = []
    for p in products:
        amount = None
        price_obj = (p.get("prices") or {}).get("price") or {}
        if "amount" in price_obj and price_obj["amount"] is not None:
            amount = price_obj["amount"] / 100.0  # Jumbo prices are in cents
        results.append(PriceResult(
            chain="Jumbo",
            name=p.get("title"),
            unitSize=p.get("quantity"),
            price=amount,
        ))
    return results


@app.get("/search", response_model=SearchResponse)
def search(
    query: str = Query(..., min_length=1),
    chain: str = Query("all", pattern="^(all|ah|jumbo)$"),
    size: int = Query(10, ge=1, le=50),
):
    """Search products by name across the requested chain(s)."""
    results: List[PriceResult] = []
    errors: List[str] = []

    sources = {"ah": _search_ah, "jumbo": _search_jumbo}
    selected = sources if chain == "all" else {chain: sources[chain]}

    # Run chains in parallel, each bounded by CHAIN_TIMEOUT_S. We deliberately do
    # NOT use the executor as a context manager: its __exit__ calls
    # shutdown(wait=True), which would block until a hung chain (e.g. Jumbo)
    # finishes. shutdown(wait=False) lets us return promptly; the stuck thread
    # dies on its own.
    executor = ThreadPoolExecutor(max_workers=len(selected))
    try:
        futures = {name: executor.submit(fn, query, size) for name, fn in selected.items()}
        for name, future in futures.items():
            try:
                results.extend(future.result(timeout=CHAIN_TIMEOUT_S))
            except FutureTimeout:
                logger.warning("search timed out for %s", name)
                errors.append(f"{name}: timed out after {CHAIN_TIMEOUT_S}s")
            except Exception as exc:  # one chain failing should not kill the request
                logger.warning("search failed for %s: %s", name, exc)
                errors.append(f"{name}: {exc}")
    finally:
        executor.shutdown(wait=False, cancel_futures=True)

    results.sort(key=lambda r: (r.price is None, r.price or 0))
    return SearchResponse(query=query, count=len(results), results=results, errors=errors)
