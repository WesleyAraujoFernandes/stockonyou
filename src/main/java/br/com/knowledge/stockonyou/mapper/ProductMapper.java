package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;
import br.com.knowledge.stockonyou.dto.request.ProductRequest;
import br.com.knowledge.stockonyou.dto.response.ProductResponse;
import br.com.knowledge.stockonyou.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequest dto);

    ProductRequest toRequest(Product product);

    ProductResponse toResponse(Product product);
}