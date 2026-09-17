package br.com.knowledge.stockonyou.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.ItemVendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaResponseDTO;
import br.com.knowledge.stockonyou.api.model.StatusVenda;
import br.com.knowledge.stockonyou.api.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {
    private final VendaService vendaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<VendaResponseDTO>> listarComFiltros(
        @RequestParam(required = false) String clienteNome, 
        @RequestParam(required = false) StatusVenda status, 
        @RequestParam(required = false) String dataInicio, 
        @RequestParam(required = false) String dataFim, 
        @PageableDefault(page =0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(vendaService.listarComFiltros(clienteNome, status, dataInicio, dataFim, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> realizarVenda(@Valid @RequestBody VendaRequestDTO dto) {
        VendaResponseDTO response = vendaService.realizarVenda(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cliente/{clienteId}/aberta")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> buscarComandaAberta(@PathVariable Long clienteId) {
        VendaResponseDTO comanda = vendaService.buscarComandaAberta(clienteId);
        if (comanda == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comanda);
    }

    @GetMapping("/comandas")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<VendaResponseDTO>> listarComandasAbertas() {
        return ResponseEntity.ok(vendaService.listarComandasAbertas());
    }

    @PutMapping("/cliente/{clienteId}/comanda")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> atualizarComanda(
        @PathVariable Long clienteId,
        @Valid @RequestBody ItemVendaRequestDTO dto) {
            return ResponseEntity.ok(vendaService.atualizarComandaAberta(clienteId, dto));
        }

    @PutMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> concluirComanda(@PathVariable Long id, StatusVenda status) {
        return ResponseEntity.ok(vendaService.concluirComanda(id, status));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> cancelarComanda(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.cancelarComanda(id));
    }

    @PutMapping("/{id}/pagamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> registrarPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.registrarPagamento(id));
    }
}
