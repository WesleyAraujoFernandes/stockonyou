package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.com.knowledge.stockonyou.dto.request.ProductRequest;
import br.com.knowledge.stockonyou.dto.response.ProductResponse;
import br.com.knowledge.stockonyou.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Request → Entity
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest dto);

    // Update com MappingTarget
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(
            ProductRequest dto,
            @org.mapstruct.MappingTarget Product product);

    // Entity → Response
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "name")
    ProductResponse toResponse(Product product);

    // Entity → Request (se realmente precisar)
    @Mapping(source = "category.id", target = "categoryId")
    ProductRequest toRequest(Product product);
}