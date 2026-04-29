package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;

import br.com.knowledge.stockonyou.dto.request.CategoryRequest;
import br.com.knowledge.stockonyou.dto.response.CategoryResponse;
import br.com.knowledge.stockonyou.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest dto);

    CategoryRequest toRequest(Category category);

    CategoryResponse toResponse(Category category);

}
