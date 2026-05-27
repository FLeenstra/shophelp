package nl.folkertleenstra.shophelp.dto;

import java.util.List;

/** Mirrors the price-service /search response. */
public record LiveSearchResponse(
        String query,
        int count,
        List<LivePrice> results,
        List<String> errors
) {
}
