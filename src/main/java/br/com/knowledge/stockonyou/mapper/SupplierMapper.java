package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.Mapper;
import br.com.knowledge.stockonyou.dto.request.SupplierRequest;
import br.com.knowledge.stockonyou.dto.response.SupplierResponse;
import br.com.knowledge.stockonyou.model.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    Supplier toEntity(SupplierRequest dto);

    SupplierRequest toRequest(Supplier supplier);

    SupplierResponse toResponse(Supplier supplier);
}
