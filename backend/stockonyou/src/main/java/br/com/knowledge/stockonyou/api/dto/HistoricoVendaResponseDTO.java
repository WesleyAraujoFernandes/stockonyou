package br.com.knowledge.stockonyou.api.dto;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;

public record HistoricoVendaResponseDTO(
    Page<VendaResponseDTO> pagina,
    BigDecimal totalFaturado,
    BigDecimal totalPendente
) {

}
