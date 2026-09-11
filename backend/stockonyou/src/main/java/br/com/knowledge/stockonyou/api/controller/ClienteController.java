package br.com.knowledge.stockonyou.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.ClienteRequestDTO;
import br.com.knowledge.stockonyou.api.dto.ClienteResponseDTO;
import br.com.knowledge.stockonyou.api.model.Cliente;
import br.com.knowledge.stockonyou.api.repository.ClienteRepository;
import br.com.knowledge.stockonyou.api.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listarTodos(
        @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.ok(clienteService.listarTodos(pageable));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<ClienteResponseDTO>> autocomplete(@RequestParam String termo) {
        List<ClienteResponseDTO> clientes = clienteRepository.findByNomeContainingIgnoreCase(termo)
                .stream()
                .map(cliente -> new ClienteResponseDTO(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone()))
                .toList();
        return ResponseEntity.ok(clientes);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<?> cadastrarRapido(@RequestBody ClienteResponseDTO dto) {
        if (clienteRepository.existsByNomeIgnoreCase(dto.nome().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Já existe um cliente cadastrado com o nome: " + dto.nome()));
        }
        Cliente cliente = Cliente.builder()
            .nome(dto.nome().trim())
            .email(dto.email())
            .telefone(dto.telefone())
            .build();
        Cliente salvo = clienteRepository.save(cliente);
        ClienteResponseDTO response = new ClienteResponseDTO(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getTelefone());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.atualizar(id, dto));
    }
}
