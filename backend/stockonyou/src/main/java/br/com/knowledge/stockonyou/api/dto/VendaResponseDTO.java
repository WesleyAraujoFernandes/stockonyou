package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.knowledge.stockonyou.api.model.Venda;

public record VendaResponseDTO(
    Long id,
    LocalDateTime dataVenda,
    String clienteNome,
    BigDecimal valorTotal,
    String usuarioNome,
    List<ItemVendaResponseDTO> itens
) {
    public static VendaResponseDTO fromEntity(Venda venda) {
        return new VendaResponseDTO(
            venda.getId(),
            venda.getDataVenda(),
            venda.getClienteNome(),
            venda.getValorTotal(),
            venda.getUsuarioNome(),
            venda.getItens().stream().map(ItemVendaResponseDTO::fromEntity).toList()
        );
    }
}
