package br.com.knowledge.stockonyou.dto.request;

import br.com.knowledge.stockonyou.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotBlank String name,
        Category category,
        String unit,
        @NotNull Double purchasePrice,
        @NotNull Double salePrice,
        @NotNull Integer stockQuantity,
        @NotNull Integer minimumStock) {
}