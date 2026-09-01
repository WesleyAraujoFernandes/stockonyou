package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.Categoria;
import java.time.LocalDateTime;

public record CategoriaResponseDTO(
        Long id,
        String nome,
        String descricao,
        LocalDateTime dataCriacao) {
    public static CategoriaResponseDTO fromEntity(Categoria categoria) {
        if (categoria == null)
            return null;
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getDataCriacao());
    }
}