package br.com.knowledge.stockonyou.dto.response;

public record ProductResponse(Long id,
        String name,
        String category,
        String unit,
        Double purchasePrice,
        Double salePrice,
        Integer stockQuantity,
        Integer minimumStock) {

}
