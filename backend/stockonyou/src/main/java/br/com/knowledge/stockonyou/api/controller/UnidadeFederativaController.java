package br.com.knowledge.stockonyou.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.UnidadeFederativaResponseDTO;
import br.com.knowledge.stockonyou.api.service.UnidadeFederativaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/uf")
@RequiredArgsConstructor
public class UnidadeFederativaController {
    private final UnidadeFederativaService unidadeFederativaService;

    @GetMapping
    public ResponseEntity<Page<UnidadeFederativaResponseDTO>> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(unidadeFederativaService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeFederativaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(unidadeFederativaService.buscarPorId(id));
    }

}
