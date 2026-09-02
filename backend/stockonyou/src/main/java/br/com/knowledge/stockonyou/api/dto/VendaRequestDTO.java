package br.com.knowledge.stockonyou.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record VendaRequestDTO(
    String clienteNome,

    @NotEmpty(message = "A venda deve conter pelo menos um item.")
    @Valid
    List<ItemVendaRequestDTO> itens

) {

}

