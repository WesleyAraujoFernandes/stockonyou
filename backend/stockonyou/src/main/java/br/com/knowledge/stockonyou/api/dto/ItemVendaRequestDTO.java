package br.com.knowledge.stockonyou.api.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemVendaRequestDTO(
    @NotNull(message = "O ID do produto é obrigatória.")
    Long produtoId,
    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    Integer quantidade
) {

}