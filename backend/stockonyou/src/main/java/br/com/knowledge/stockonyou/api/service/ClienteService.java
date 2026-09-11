package br.com.knowledge.stockonyou.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.dto.ClienteRequestDTO;
import br.com.knowledge.stockonyou.api.dto.ClienteResponseDTO;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Cliente;
import br.com.knowledge.stockonyou.api.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;

    public Page<ClienteResponseDTO> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(ClienteResponseDTO::fromEntity);
    }

    @Transactional 
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente nao encontrado com o ID:"+id));
        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        return ClienteResponseDTO.fromEntity(repository.save(cliente));
    }

    @Transactional 
    public void deletar(Long id) {
        if (repository.findById(id) != null) {
            throw new ResourceNotFoundException("Cliente não encontrado com o ID:"+id);
        }
        repository.deleteById(id);

    }
}
