package br.com.knowledge.stockonyou.mapper;

import br.com.knowledge.stockonyou.dto.ProductDTO;
import br.com.knowledge.stockonyou.model.Product;

public class ProductMapperImpl implements ProductMapper {
    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setUnit(product.getUnit());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setMinimumStock(product.getMinimumStock());
        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setUnit(dto.getUnit());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSalePrice(dto.getSalePrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setMinimumStock(dto.getMinimumStock());
        return product;
    }
}
