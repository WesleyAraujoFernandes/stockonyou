package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.knowledge.stockonyou.dto.response.CommandItemResponse;
import br.com.knowledge.stockonyou.dto.response.CommandResponse;
import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandItem;

@Mapper(componentModel = "spring")
public interface CommandMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    CommandItemResponse toItemResponse(CommandItem item);

    CommandResponse toResponse(Command command);
}
