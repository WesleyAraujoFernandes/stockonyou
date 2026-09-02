package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;

import br.com.knowledge.stockonyou.api.model.ItemVenda;

public record ItemVendaResponseDTO(
        Long id,
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal) {
    public static ItemVendaResponseDTO fromEntity(ItemVenda item) {
        return new ItemVendaResponseDTO(item.getId(), item.getProduto().getId(), item.getProduto().getNome(),
                item.getQuantidade(), item.getPrecoUnitario(), item.getSubtotal());
    }
}
