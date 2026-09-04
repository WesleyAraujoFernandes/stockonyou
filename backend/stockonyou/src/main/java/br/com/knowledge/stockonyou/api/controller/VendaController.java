package br.com.knowledge.stockonyou.api.controller;

import br.com.knowledge.stockonyou.api.repository.VendaRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final VendaRepository vendaRepository;
    private final VendaService vendaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> realizarVenda(@Valid @RequestBody VendaRequestDTO dto) {
        VendaResponseDTO response = vendaService.realizarVenda(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cliente/{clienteId}/aberta")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> buscarComandaAberta(@PathVariable Long clienteId) {
        return vendaRepository.findByClienteIdAndStatus(clienteId, StatusVenda.ABERTA).map(venda -> ResponseEntity.ok(VendaResponseDTO.fromEntity(venda)))
                .orElse(ResponseEntity.noContent().build());
                
    }

    @GetMapping("/comandas")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<VendaResponseDTO>> listarComandasAbertas() {
        return ResponseEntity.ok(vendaService.listarComandasAbertas());
    }

    @PutMapping("/cliente/{clienteId}/comanda")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> atualizarComdanda(
        @PathVariable Long clienteId,
        @Valid @RequestBody ItemVendaRequestDTO dto) {
            return ResponseEntity.ok(vendaService.atualizarComandaAberta(clienteId, dto));
        }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> finalizarComanda(@PathVariable Long id, StatusVenda status) {
        return ResponseEntity.ok(vendaService.finalizarComanda(id, status));
    }
}
