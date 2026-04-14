package br.com.knowledge.stockonyou.dto.request;

import br.com.knowledge.stockonyou.model.MovementType;

public record StockMovementRequest(
        Long productId,
        MovementType type,
        Integer quantity,
        String reason,
        String reference) {

}
