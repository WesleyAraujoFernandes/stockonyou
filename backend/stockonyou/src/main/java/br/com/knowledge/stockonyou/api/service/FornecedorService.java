package br.com.knowledge.stockonyou.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.util.Resource;

import br.com.knowledge.stockonyou.api.dto.FornecedorRequestDTO;
import br.com.knowledge.stockonyou.api.dto.FornecedorResponseDTO;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Cidade;
import br.com.knowledge.stockonyou.api.model.Fornecedor;
import br.com.knowledge.stockonyou.api.repository.CidadeRepository;
import br.com.knowledge.stockonyou.api.repository.FornecedorRepository;
import br.com.knowledge.stockonyou.api.specification.FornecedorSpecification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FornecedorService {
    private final FornecedorRepository repository;
    private final CidadeRepository cidadeRepository;

    @Transactional(readOnly = true)
    public Page<FornecedorResponseDTO> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(FornecedorResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public FornecedorResponseDTO buscarPorId(Long id) {
        Fornecedor fornecedor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor nao encontrado com o ID: " + id));
        return FornecedorResponseDTO.fromEntity(fornecedor);
    }

    @Transactional(readOnly = true)
    public Page<FornecedorResponseDTO> buscarDinamica(String nome, Pageable pageable) {
        Specification<Fornecedor> spec = FornecedorSpecification.comFiltros(nome);
        return repository.findAll(spec, pageable)
            .map(FornecedorResponseDTO::fromEntity);
    }

    @Transactional 
    public FornecedorResponseDTO criar(FornecedorRequestDTO dto) {
        Cidade cidade = cidadeRepository.findById(dto.cidadeId())
            .orElseThrow(() -> new ResourceNotFoundException("Cidade não encontrada com o ID: " + dto.cidadeId()));
        Fornecedor fornecedor = Fornecedor.builder()
            .nome(dto.nome())
            .cep(dto.cep())
            .cidade(cidade)
            .cnpj(dto.cnpj())
            .email(dto.email())
            .endereco(dto.endereco())
            .observacao(dto.observacao())
            .telefone(dto.telefone())
            .build();
        return FornecedorResponseDTO.fromEntity(repository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponseDTO atualizar(Long id, FornecedorRequestDTO dto) {
        Cidade cidade = cidadeRepository.findById(dto.cidadeId())
            .orElseThrow(() -> new ResourceNotFoundException("Cidade não encontrada com o ID: " + dto.cidadeId()));
        Fornecedor fornecedor = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado com o ID: " + id));
        fornecedor.setCep(dto.cep());
        fornecedor.setCidade(cidade);
        fornecedor.setCnpj(dto.cnpj());
        fornecedor.setEmail(dto.email());
        fornecedor.setEndereco(dto.endereco());
        fornecedor.setNome(dto.nome());
        fornecedor.setObservacao(dto.observacao());
        fornecedor.setTelefone(dto.telefone());
        return FornecedorResponseDTO.fromEntity(repository.save(fornecedor));
    }

    @Transactional 
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Fornecedor nao encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }
}
