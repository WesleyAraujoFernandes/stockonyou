package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import br.com.knowledge.stockonyou.dto.request.ProductRequest;
import br.com.knowledge.stockonyou.dto.response.ProductResponse;
import br.com.knowledge.stockonyou.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest dto);

    // Update com MappingTarget
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(
            ProductRequest dto,
            @org.mapstruct.MappingTarget Product product);

    // Entity → Response
    @Mapping(target = "categoryId", expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(source = "category.categoryName", target = "categoryName")
    ProductResponse toResponse(Product product);

}