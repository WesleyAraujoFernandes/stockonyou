package br.com.knowledge.stockonyou.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.api.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByVendaId(Long vendaId);
}
