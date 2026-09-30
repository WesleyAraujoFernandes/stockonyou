package br.com.knowledge.stockonyou.api.repository;

import java.math.BigDecimal;

import br.com.knowledge.stockonyou.api.dto.VendaTotaisProjection;
import br.com.knowledge.stockonyou.api.model.StatusVenda;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import br.com.knowledge.stockonyou.api.model.Venda;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VendaRepositoryCustomImpl implements VendaRepositoryCustom {
    private final EntityManager entityManager;

    @Override
    public VendaTotaisProjection calcularTotais(Specification<Venda> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Venda> root = query.from(Venda.class);
        Predicate predicate = specification.toPredicate(
            root,
            query,
            cb
        );

        Expression<BigDecimal> totalFaturado = cb.sum(
            cb.<BigDecimal>selectCase()
                .when(
                    cb.equal(root.get("status"), StatusVenda.PAGO),
                    root.get("valorTotal")
                )
                .otherwise(BigDecimal.ZERO)
        );

        Expression<BigDecimal> totalPendente = cb.sum(
            cb.<BigDecimal>selectCase()
                .when(
                    cb.equal(root.get("status"), StatusVenda.PENDENTE),
                    root.get("valorTotal")
                )
                .otherwise(BigDecimal.ZERO)
        );

        query.multiselect(
            totalFaturado,
            totalPendente
        );

        query.where(predicate);

        Object[] resultado = entityManager
            .createQuery(query)
            .getSingleResult();
        
        return new VendaTotaisProjection(
            resultado[0] != null ? (BigDecimal) resultado[0] : BigDecimal.ZERO,
            resultado[1] != null ? (BigDecimal) resultado[1] : BigDecimal.ZERO
        );
    }
}
