package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.knowledge.stockonyou.api.model.FormaPagamento;
import br.com.knowledge.stockonyou.api.model.Pagamento;

public record PagamentoResponseDTO(
    Long id,
    BigDecimal valor,
    FormaPagamento formaPagamento,
    LocalDateTime dataPagamento,
    String usuarioNome) {
    public static PagamentoResponseDTO fromEntity(Pagamento pagamento) {
        return new PagamentoResponseDTO(
            pagamento.getId(),
            pagamento.getValor(),
            pagamento.getFormaPagamento(),
            pagamento.getDataPagamento(),
            pagamento.getUsuarioNome());
    }
}
