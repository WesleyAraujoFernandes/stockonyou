package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;

import br.com.knowledge.stockonyou.api.model.FormaPagamento;

public record PagamentoRequestDTO(
    BigDecimal valor,
    FormaPagamento formaPagamento
) {
    
}
