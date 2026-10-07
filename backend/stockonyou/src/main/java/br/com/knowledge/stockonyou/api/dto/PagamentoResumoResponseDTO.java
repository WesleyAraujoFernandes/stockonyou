package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;

public record PagamentoResumoResponseDTO(
    BigDecimal valorTotal,
    BigDecimal totalPago,
    BigDecimal saldoRestante
) {

}
