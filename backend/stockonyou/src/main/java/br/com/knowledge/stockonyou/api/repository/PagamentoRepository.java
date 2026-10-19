package br.com.knowledge.stockonyou.api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.knowledge.stockonyou.api.model.Pagamento;

import br.com.knowledge.stockonyou.api.model.StatusVenda;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByVendaId(Long vendaId);

    @Query("SELECT COALESCE(SUM(p.valor),0) FROM Pagamento p WHERE p.venda.id = :vendaId")
    BigDecimal calcularTotalPago(@Param("vendaId") Long vendaId);

    @Query("""
            SELECT COALESCE(SUM(p.valor), 0)
            FROM Pagamento p
            WHERE p.dataPagamento >= :dataInicio
              AND p.dataPagamento < :dataFim
              AND p.venda.status <> :statusCancelada
            """)
    BigDecimal calcularTotalRecebido(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("statusCancelada") StatusVenda statusCancelada);

}
