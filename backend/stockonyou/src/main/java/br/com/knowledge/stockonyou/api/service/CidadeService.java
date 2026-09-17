package br.com.knowledge.stockonyou.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.dto.CidadeResponseDTO;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.repository.CidadeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CidadeService {
    private final CidadeRepository repository;

    public Page<CidadeResponseDTO> listarTodos(Pageable pageable) {
        return repository.findAll(pageable)
            .map(CidadeResponseDTO::fromEntity); 
    }
    
    public CidadeResponseDTO buscarPorId(Long id) {
        return CidadeResponseDTO.fromEntity(repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cidade nao encontrada com o ID: " + id)));
    }
}
