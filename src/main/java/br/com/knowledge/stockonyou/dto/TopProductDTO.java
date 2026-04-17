package br.com.knowledge.stockonyou.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopProductDTO {
    private Long productId;
    private String productName;
    private Long quantity;
    private BigDecimal totalAmount;
}
