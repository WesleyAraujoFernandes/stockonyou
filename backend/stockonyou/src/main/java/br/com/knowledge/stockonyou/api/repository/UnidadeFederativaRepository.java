package br.com.knowledge.stockonyou.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.knowledge.stockonyou.api.model.UnidadeFederativa;

public interface UnidadeFederativaRepository extends JpaRepository<UnidadeFederativa, Long>, JpaSpecificationExecutor<UnidadeFederativa> {
    UnidadeFederativa findBySiglaContainingIgnoreCase(String sigla);
    UnidadeFederativa findByNomeContainingIgnoreCase(String nome);
}
