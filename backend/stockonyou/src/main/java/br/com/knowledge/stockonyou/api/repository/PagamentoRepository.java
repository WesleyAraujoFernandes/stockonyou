package br.com.knowledge.stockonyou.api.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.knowledge.stockonyou.api.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByVendaId(Long vendaId);

    @Query("SELECT COALESCE(SUM(p.valor),0) FROM Pagamento p WHERE p.venda.id = :vendaId")
    BigDecimal calcularTotalPago(@Param("vendaId") Long vendaId);
}
