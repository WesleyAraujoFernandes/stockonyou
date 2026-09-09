package br.com.knowledge.stockonyou.api.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.knowledge.stockonyou.api.model.StatusVenda;
import br.com.knowledge.stockonyou.api.model.Venda;
import jakarta.persistence.criteria.Predicate;

public class VendaSpecification {

    public static Specification<Venda> comFiltros(String clienteNome, StatusVenda status, String dataInicio, String dataFim) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 1. Filtro por Nome do Cliente (parcial e ignorando maiúsculas/minúsculas)
            if (clienteNome != null && !clienteNome.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("clienteNome")),
                        "%" + clienteNome.toLowerCase().trim() + "%"));
            }

            // 2. Filtro por Status da Venda
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 3. Filtro por Data de Início (A partir das 00:00:00 daquele dia)
            if (dataInicio != null && !dataInicio.isBlank()) {
                LocalDate dataIni = LocalDate.parse(dataInicio.trim(), formatter);
                LocalDateTime dataHoraIni = dataIni.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataVenda"), dataHoraIni));
            }

            // 4. Filtro por Data de Fim (Até as 23:59:59 daquele dia)
            if (dataFim != null && !dataFim.isBlank()) {
                LocalDate dataF = LocalDate.parse(dataFim.trim(), formatter);
                LocalDateTime dataHoraFim = dataF.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataVenda"), dataHoraFim));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}