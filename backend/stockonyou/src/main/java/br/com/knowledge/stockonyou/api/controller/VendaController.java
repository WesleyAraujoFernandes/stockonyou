package br.com.knowledge.stockonyou.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.VendaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.VendaResponseDTO;
import br.com.knowledge.stockonyou.api.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {
    private final VendaService vendaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<VendaResponseDTO> realizarVenda(@Valid @RequestBody VendaRequestDTO dto) {
        VendaResponseDTO response = vendaService.realizarVenda(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
