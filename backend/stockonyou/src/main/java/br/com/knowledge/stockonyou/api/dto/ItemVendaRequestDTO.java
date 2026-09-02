package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemVendaRequestDTO(
    @NotNull(message = "O ID do produto é obrigatório.")
    Long produtoId,
    @NotNull(message = "A quantidade é obrigatório.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    Integer quantidade,
    @NotNull(message = "O preço unitário é obrigatório.")
    @Positive(message = "O preço unitário deve ser maior que zero.")
    BigDecimal precoUnitario
) {

}