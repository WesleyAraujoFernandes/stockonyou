package br.com.knowledge.stockonyou.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.api.dto.ClienteDTO;
import br.com.knowledge.stockonyou.api.model.Cliente;
import br.com.knowledge.stockonyou.api.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteRepository clienteRepository;

    @GetMapping("/autocomplete")
    public ResponseEntity<List<ClienteDTO>> autocomplete(@RequestParam String termo) {
        List<ClienteDTO> clientes = clienteRepository.findByNomeContainingIgnoreCase(termo)
                .stream()
                .map(cliente -> new ClienteDTO(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone()))
                .toList();
        return ResponseEntity.ok(clientes);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<?> cadastrarRapido(@RequestBody ClienteDTO dto) {
        if (clienteRepository.existsByNomeIgnoreCase(dto.nome().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Já existe um cliente cadastrado com o nome: " + dto.nome()));
        }
        Cliente cliente = Cliente.builder()
            .nome(dto.nome().trim())
            .email(dto.email())
            .telefone(dto.telefone())
            .build();
        Cliente salvo = clienteRepository.save(cliente);
        ClienteDTO response = new ClienteDTO(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getTelefone());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
