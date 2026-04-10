package br.com.knowledge.stockonyou.mapper;

import org.mapstruct.factory.Mappers;

import br.com.knowledge.stockonyou.dto.SupplierDTO;
import br.com.knowledge.stockonyou.model.Supplier;

public interface SupplierMapper {
    SupplierMapper INSTANCE = Mappers.getMapper(SupplierMapper.class);

    SupplierDTO toDTO(Supplier supplier);

    Supplier toEntity(SupplierDTO dto);
}
