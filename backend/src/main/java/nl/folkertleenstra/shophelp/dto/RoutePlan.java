package nl.folkertleenstra.shophelp.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The full plan: an ordered list of stores to visit to buy each item at its
 * cheapest store, the estimated basket cost, and the total travel distance.
 */
public record RoutePlan(
        List<RouteStop> stops,
        BigDecimal estimatedTotal,
        double totalDistanceKm
) {
}
