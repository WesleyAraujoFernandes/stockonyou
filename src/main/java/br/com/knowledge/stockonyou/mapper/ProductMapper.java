package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import br.com.knowledge.stockonyou.dto.ProductDTO;
import br.com.knowledge.stockonyou.model.Product;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDTO toDTO(Product product);

    Product toEntity(ProductDTO dto);
}
