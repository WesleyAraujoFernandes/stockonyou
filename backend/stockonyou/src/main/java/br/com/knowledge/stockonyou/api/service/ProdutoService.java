package br.com.knowledge.stockonyou.api.service;

import br.com.knowledge.stockonyou.api.dto.ProdutoRequestDTO;
import br.com.knowledge.stockonyou.api.dto.ProdutoResponseDTO;
import br.com.knowledge.stockonyou.api.exception.DuplicateResourceException;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Categoria;
import br.com.knowledge.stockonyou.api.model.Produto;
import br.com.knowledge.stockonyou.api.repository.CategoriaRepository;
import br.com.knowledge.stockonyou.api.repository.ProdutoRepository;
import br.com.knowledge.stockonyou.api.specification.ProdutoSpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarTodos(Pageable pageable) {
        return produtoRepository.findAll(pageable)
                .map(ProdutoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
        return ProdutoResponseDTO.fromEntity(produto);
    }

    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        if (produtoRepository.existsByCodigoBarras(dto.codigoBarras())) {
            throw new DuplicateResourceException("Código de barras já cadastrado: " + dto.codigoBarras());
        }

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Categoria não encontrada com id: " + dto.categoriaId()));

        Produto produto = Produto.builder()
                .nome(dto.nome())
                .codigoBarras(dto.codigoBarras())
                .quantidade(dto.quantidade())
                .preco(dto.preco())
                .categoria(categoria)
                .build();

        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponseDTO atualizarEstoque(Long id, Integer novaQuantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        produto.setQuantidade(novaQuantidade);
        return ProdutoResponseDTO.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto não encontrado com id: " + id);
        }
        produtoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> buscarPorCategoria(Long categoriaId, Pageable pageable) {
        // 1. Valida se a categoria existe para lançar 404 caso não seja encontrada
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResourceNotFoundException("Categoria não encontrada com id: " + categoriaId);
        }

        // 2. Busca os produtos e mapeia para DTOs
        return produtoRepository.findByCategoriaId(categoriaId, pageable)
                .map(ProdutoResponseDTO::fromEntity);

    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> buscarDinamica(
            String nome,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Long categoriaId,
            Pageable pageable) {
        Specification<Produto> spec = ProdutoSpecification.comFiltros(nome, precoMin, precoMax, categoriaId);
        return produtoRepository.findAll(spec, pageable)
                .map(ProdutoResponseDTO::fromEntity);
    }
}