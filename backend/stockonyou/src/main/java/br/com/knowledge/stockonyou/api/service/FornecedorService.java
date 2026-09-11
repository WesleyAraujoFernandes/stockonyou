package br.com.knowledge.stockonyou.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.api.model.Fornecedor;
import br.com.knowledge.stockonyou.api.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FornecedorService {
    private final FornecedorRepository repository;

    public List<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Fornecedor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor nao encontrado com o ID: " + id));
    }
}
