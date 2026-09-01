package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String codigoBarras,
        Integer quantidade,
        BigDecimal preco,
        CategoriaResponseDTO categoria,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao) {
    public static ProdutoResponseDTO fromEntity(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getCodigoBarras(),
                produto.getQuantidade(),
                produto.getPreco(),
                CategoriaResponseDTO.fromEntity(produto.getCategoria()),
                produto.getDataCriacao(),
                produto.getDataAtualizacao());
    }
}