package br.com.knowledge.stockonyou.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.model.Cidade;
import br.com.knowledge.stockonyou.api.repository.CidadeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CidadeService {
    private final CidadeRepository cidadeRepository;

    public List<Cidade> listarTodos() {
        return cidadeRepository.findAll();
    }

    public Cidade buscarPorId(Long id) {
        return cidadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cidade nao encontrada com o ID: " + id));
    }
}
