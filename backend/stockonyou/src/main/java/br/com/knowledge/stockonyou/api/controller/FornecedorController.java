package br.com.knowledge.stockonyou.api.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.CidadeResponseDTO;
import br.com.knowledge.stockonyou.api.dto.FornecedorRequestDTO;
import br.com.knowledge.stockonyou.api.dto.FornecedorResponseDTO;
import br.com.knowledge.stockonyou.api.repository.FornecedorRepository;
import br.com.knowledge.stockonyou.api.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {
    private final FornecedorService fornecedorService;

    @GetMapping
    public ResponseEntity<Page<FornecedorResponseDTO>> listarTodos(
            @RequestParam(required = false) String nome,
            @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(fornecedorService.buscarDinamica(nome, pageable));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<FornecedorResponseDTO>> autocomplete(@RequestParam String termo) {
        return ResponseEntity.ok(fornecedorService.autocomplete(termo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<FornecedorResponseDTO> criar(
        @Valid @RequestBody FornecedorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.criar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<FornecedorResponseDTO> atualizar(
        @PathVariable Long id, @Valid @RequestBody FornecedorRequestDTO dto ) {
            return ResponseEntity.ok(fornecedorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fornecedorService.deletar(id); 
        return ResponseEntity.noContent().build();
    }

}
