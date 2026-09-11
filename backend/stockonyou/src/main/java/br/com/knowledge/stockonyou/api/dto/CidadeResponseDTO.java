package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.Cidade;

public record CidadeResponseDTO(
    Long id,
    String nome
) {
    public static CidadeResponseDTO fromEntity(Cidade cidade) { 
        return new CidadeResponseDTO(cidade.getId(), cidade.getNome());
    }
}
