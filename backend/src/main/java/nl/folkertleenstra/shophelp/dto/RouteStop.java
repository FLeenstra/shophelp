package nl.folkertleenstra.shophelp.dto;

import java.math.BigDecimal;
import java.util.List;

/** One stop on the planned route: a store and what to buy there (the cheapest items). */
public record RouteStop(
        int order,
        Long storeId,
        String storeName,
        double latitude,
        double longitude,
        BigDecimal subtotal,
        List<String> items
) {
}
