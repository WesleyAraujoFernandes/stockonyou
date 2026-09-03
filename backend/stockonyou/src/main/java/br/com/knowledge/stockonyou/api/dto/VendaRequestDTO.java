package br.com.knowledge.stockonyou.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VendaRequestDTO(
    @NotNull(message = "O ID do cliente é obrigatório.")
    Long clienteId,

    @NotEmpty(message = "A venda deve conter pelo menos um item.")
    @Valid
    List<ItemVendaRequestDTO> itens

) {

}

