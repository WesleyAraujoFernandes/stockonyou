package br.com.knowledge.stockonyou.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.knowledge.stockonyou.api.dto.PagamentoRequestDTO;
import br.com.knowledge.stockonyou.api.dto.PagamentoResponseDTO;
import br.com.knowledge.stockonyou.api.dto.PagamentoResumoResponseDTO;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Pagamento;
import br.com.knowledge.stockonyou.api.model.StatusVenda;
import br.com.knowledge.stockonyou.api.model.Venda;
import br.com.knowledge.stockonyou.api.repository.PagamentoRepository;
import br.com.knowledge.stockonyou.api.repository.VendaRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PagamentoService {
    private final PagamentoRepository pagamentoRepository;
    private final VendaRepository vendaRepository;

    @Transactional 
    public PagamentoResponseDTO registrarPagamento(Long vendaId, PagamentoRequestDTO request) {
        Venda venda = buscarVenda(vendaId);
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalStateException(
                "Não é permitido registrar pagamento em uma venda cancelada."
            );
        }

        if (request.formaPagamento() == null) {
            throw new IllegalArgumentException("A forma de pagamento deve ser informada.");
        }
        
        BigDecimal saldoRestante = calcularSaldoRestante(venda);

        if (saldoRestante.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("A venda já está totalmente paga.");
        }
        
        validarValorPagamento(request.valor(), saldoRestante);

        Pagamento pagamento = Pagamento.builder()
            .valor(request.valor())
            .formaPagamento(request.formaPagamento())
            .dataPagamento(LocalDateTime.now())
            .usuarioNome(venda.getUsuarioNome())
            .venda(venda)
            .build();
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);
        return PagamentoResponseDTO.fromEntity(pagamentoSalvo);
    } 

    @Transactional 
    public PagamentoResumoResponseDTO consultarResumo(Long vendaId) {
        Venda venda = buscarVenda(vendaId);
        BigDecimal totalPago = pagamentoRepository.calcularTotalPago(venda.getId());
        BigDecimal saldoRestante = venda.getValorTotal().subtract(totalPago);
        return new PagamentoResumoResponseDTO(venda.getValorTotal(), totalPago, saldoRestante);
    }

    private Venda buscarVenda(Long vendaId) {
        return vendaRepository.findById(vendaId)
            .orElseThrow(() -> new ResourceNotFoundException("Venda nao encontrada com o ID: " + vendaId));
    }

    private BigDecimal calcularSaldoRestante(Venda venda) {
        BigDecimal totalPago = pagamentoRepository.calcularTotalPago(venda.getId());
        return venda.getValorTotal().subtract(totalPago);
    }

    private void validarValorPagamento(
        BigDecimal valorPagamento,
        BigDecimal saldoRestante
    ) {
        if (valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior do que zero.");
        }

        if (valorPagamento.compareTo(saldoRestante) > 0) {
            throw new IllegalArgumentException("O valor do pagamento nao pode ser maior do que o saldo restante da venda.");
        }
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPagamentos(Long vendaId) {
        buscarVenda(vendaId);
        return pagamentoRepository.findByVendaId(vendaId).stream()
            .map(PagamentoResponseDTO::fromEntity)
            .toList();
    }
}
