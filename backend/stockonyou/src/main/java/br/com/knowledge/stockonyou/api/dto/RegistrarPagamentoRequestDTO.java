package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.FormaPagamento;

public record RegistrarPagamentoRequestDTO(
    FormaPagamento formaPagamento
) {

}
