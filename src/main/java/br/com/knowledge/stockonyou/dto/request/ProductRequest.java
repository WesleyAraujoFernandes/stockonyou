package br.com.knowledge.stockonyou.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
                @NotBlank String name,
                String category,
                String unit,
                @NotNull Double purchasePrice,
                @NotNull Double salePrice,
                @NotNull Integer stockQuantity,
                @NotNull Integer minimumStock) {
}