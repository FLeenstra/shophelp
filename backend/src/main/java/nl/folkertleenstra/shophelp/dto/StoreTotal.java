package nl.folkertleenstra.shophelp.dto;

import java.math.BigDecimal;

/**
 * The cost of a whole basket at one store.
 * {@code complete} is true when the store carries every requested item.
 */
public record StoreTotal(
        Long storeId,
        String storeName,
        BigDecimal total,
        int itemsAvailable,
        int itemsRequested,
        boolean complete
) {
}
