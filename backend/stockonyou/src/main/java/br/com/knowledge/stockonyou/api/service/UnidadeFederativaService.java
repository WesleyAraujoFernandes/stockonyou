package br.com.knowledge.stockonyou.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.dto.UnidadeFederativaResponseDTO;
import br.com.knowledge.stockonyou.api.repository.UnidadeFederativaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadeFederativaService {
    private final UnidadeFederativaRepository repository;

    public Page<UnidadeFederativaResponseDTO> listarTodos(Pageable pageable) {
        return repository.findAll(pageable)
            .map(UnidadeFederativaResponseDTO::fromEntity); 
    }

    public UnidadeFederativaResponseDTO buscarPorId(Long id) {
        return UnidadeFederativaResponseDTO.fromEntity(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade Federativa nao encontrada com o ID: " + id)));
    }
}
