package br.com.knowledge.stockonyou.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.CidadeResponseDTO;
import br.com.knowledge.stockonyou.api.service.CidadeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cidades")
@RequiredArgsConstructor
public class CidadeController {
    private final CidadeService cidadeService;
    @GetMapping
    public ResponseEntity<Page<CidadeResponseDTO>> listarTodos(
        @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(cidadeService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CidadeResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cidadeService.buscarPorId(id));
    }
}
