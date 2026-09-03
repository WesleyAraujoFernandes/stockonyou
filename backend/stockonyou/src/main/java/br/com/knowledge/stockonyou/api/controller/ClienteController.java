package br.com.knowledge.stockonyou.api.controller;

import java.util.List;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')") // TODO: implementar regra de acesso
    public ResponseEntity<ClienteDTO> cadastrarRapido(@RequestBody ClienteDTO dto) {
        Cliente cliente = Cliente.builder().nome(dto.nome()).email(dto.email()).telefone(dto.telefone()).build();
        Cliente salvo = clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ClienteDTO(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getTelefone()));
    }
}
