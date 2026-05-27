package nl.folkertleenstra.shophelp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** One line in a shopping basket: a product and how many of it. */
public record BasketItem(
        @NotNull Long productId,
        @Min(1) int quantity
) {
}
