package nl.folkertleenstra.shophelp.dto;

import java.math.BigDecimal;

/** A single store's price for one product. */
public record ProductPriceView(
        Long storeId,
        String storeName,
        BigDecimal price,
        String currency
) {
}
