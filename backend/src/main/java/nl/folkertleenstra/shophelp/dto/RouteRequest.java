package nl.folkertleenstra.shophelp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** A basket plus the shopper's starting location, used to plan a map route. */
public record RouteRequest(
        @NotEmpty @Valid List<BasketItem> items,
        double startLat,
        double startLng
) {
}
