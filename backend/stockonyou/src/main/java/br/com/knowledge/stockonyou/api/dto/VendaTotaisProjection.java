package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;

public record VendaTotaisProjection(
    BigDecimal totalFaturado,
    BigDecimal totalPendente
) {

}
