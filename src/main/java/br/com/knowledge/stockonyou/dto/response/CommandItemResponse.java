package br.com.knowledge.stockonyou.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommandItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        BigDecimal subtotal,
        LocalDateTime addedAt) {

}
