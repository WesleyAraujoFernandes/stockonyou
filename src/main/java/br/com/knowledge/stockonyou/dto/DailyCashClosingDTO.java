package br.com.knowledge.stockonyou.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyCashClosingDTO {
    private BigDecimal totalAmount;
    private long totalCommands;
    private BigDecimal averageTicket;

    private BigDecimal cashTotal;
    private BigDecimal creditCardTotal;
    private BigDecimal debitCardTotal;
    private BigDecimal pixTotal;
    private BigDecimal transferTotal;
}
