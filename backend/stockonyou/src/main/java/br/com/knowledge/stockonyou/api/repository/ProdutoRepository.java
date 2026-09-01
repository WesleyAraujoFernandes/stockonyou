package br.com.knowledge.stockonyou.api.repository;

import br.com.knowledge.stockonyou.api.model.Produto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    boolean existsByCodigoBarras(String codigoBarras);

    Page<Produto> findByCategoriaId(Long categoriaId, Pageable pageable);

}