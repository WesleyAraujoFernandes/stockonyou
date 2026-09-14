package br.com.knowledge.stockonyou.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.knowledge.stockonyou.api.model.Fornecedor;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {
    List<Fornecedor> findByNomeContainingIgnoreCase(String termo);
    boolean existsByNomeIgnoreCase(String nome);
}
