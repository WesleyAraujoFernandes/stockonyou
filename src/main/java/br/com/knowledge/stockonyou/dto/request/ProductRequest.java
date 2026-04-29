package br.com.knowledge.stockonyou.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
                @NotBlank String name,
                Long categoryId,
                String unit,
                @NotNull BigDecimal purchasePrice,
                @NotNull BigDecimal salePrice,
                @NotNull Integer stockQuantity,
                @NotNull Integer minimumStock) {
}