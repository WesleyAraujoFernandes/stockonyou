package br.com.knowledge.stockonyou.api.specification;

import org.springframework.data.jpa.domain.Specification;

import br.com.knowledge.stockonyou.api.model.Fornecedor;

public class FornecedorSpecification {
    public static Specification<Fornecedor> comFiltros(String nome) {
        return (root, query, criteriaBuilder) -> {
            if (nome == null || nome.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(root.get("name"), "%" + nome + "%");
        };
    }
}
