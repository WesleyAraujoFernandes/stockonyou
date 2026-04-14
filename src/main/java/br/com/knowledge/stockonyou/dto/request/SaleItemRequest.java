package br.com.knowledge.stockonyou.dto.request;

public record SaleItemRequest(
        Long productId,
        Integer quantity) {
}
