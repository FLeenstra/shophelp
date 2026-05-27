package nl.folkertleenstra.shophelp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompareRequest(
        @NotEmpty @Valid List<BasketItem> items
) {
}
