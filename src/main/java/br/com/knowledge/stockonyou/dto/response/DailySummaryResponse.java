package br.com.knowledge.stockonyou.dto.response;

import java.math.BigDecimal;

public record DailySummaryResponse(
                BigDecimal totalSales,
                Long totalCommands,
                BigDecimal averageTicket) {

}
