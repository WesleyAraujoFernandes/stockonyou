package br.com.knowledge.stockonyou.dto.request;

public record CommandItemRequest(
        Long productId,
        Integer quantity) {

}
