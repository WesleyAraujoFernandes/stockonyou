package br.com.knowledge.stockonyou.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotBlank(message = "O nome do produto é obrigatório.") @Size(max = 120, message = "O nome não pode exceder 120 caracteres.") String nome,

        @NotBlank(message = "O código de barras é obrigatório.") String codigoBarras,

        @NotNull(message = "A quantidade é obrigatória.") @Min(value = 0, message = "A quantidade não pode ser negativa.") Integer quantidade,

        @NotNull(message = "O preço é obrigatório.") @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.") BigDecimal preco,

        @NotNull(message = "O ID da categoria é obrigatório.") Long categoriaId) {
}