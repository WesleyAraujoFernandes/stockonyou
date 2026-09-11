package br.com.knowledge.stockonyou.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.model.UnidadeFederativa;
import br.com.knowledge.stockonyou.api.repository.UnidadeFederativaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadeFederativaService {
    private final UnidadeFederativaRepository repository;

    public List<UnidadeFederativa> listarTodos() {
        return repository.findAll();
    }

    public UnidadeFederativa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade Federativa nao encontrada com o ID: " + id));
    }
}
