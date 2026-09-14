package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.UnidadeFederativa;

public record UnidadeFederativaResponseDTO(
    Long id,
    String sigla,
    String nome
) {
    public static UnidadeFederativaResponseDTO fromEntity(UnidadeFederativa unidadeFederativa) {
        return new UnidadeFederativaResponseDTO(
            unidadeFederativa.getId(),
            unidadeFederativa.getNome(),
            unidadeFederativa.getSigla()
        );
    }
}
