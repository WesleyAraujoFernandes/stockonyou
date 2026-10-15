package br.com.knowledge.stockonyou.api.dto;

import java.util.List;
import br.com.knowledge.stockonyou.api.model.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record VendaRequestDTO(
    Long clienteId,
    String clienteNome,
    FormaPagamento formaPagamento,
    @NotEmpty(message = "A venda deve conter pelo menos um item.")
    @Valid
    List<ItemVendaRequestDTO> itens
) {

}

