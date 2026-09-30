package br.com.knowledge.stockonyou.api.repository;

import org.springframework.data.jpa.domain.Specification;

import br.com.knowledge.stockonyou.api.dto.VendaTotaisProjection;

import br.com.knowledge.stockonyou.api.model.Venda;

public interface VendaRepositoryCustom {
    VendaTotaisProjection calcularTotais(Specification<Venda> specification);
}
