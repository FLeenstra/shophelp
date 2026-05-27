package nl.folkertleenstra.shophelp.dto;

import java.math.BigDecimal;

/** A live product price returned by the price-service (one supermarket chain). */
public record LivePrice(
        String chain,
        String name,
        String unitSize,
        BigDecimal price,
        String currency
) {
}
