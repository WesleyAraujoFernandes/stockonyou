package br.com.knowledge.stockonyou.dto.response;

import br.com.knowledge.stockonyou.model.Category;

public record ProductResponse(Long id,
                String name,
                Category category,
                String unit,
                Double purchasePrice,
                Double salePrice,
                Integer stockQuantity,
                Integer minimumStock) {

}
