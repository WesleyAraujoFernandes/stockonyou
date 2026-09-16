package br.com.knowledge.stockonyou.api.service;

import br.com.knowledge.stockonyou.api.dto.CategoriaRequestDTO;
import br.com.knowledge.stockonyou.api.dto.CategoriaResponseDTO;
import br.com.knowledge.stockonyou.api.exception.DuplicateResourceException;
import br.com.knowledge.stockonyou.api.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.api.model.Categoria;
import br.com.knowledge.stockonyou.api.repository.CategoriaRepository;
import br.com.knowledge.stockonyou.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
   
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));
        return CategoriaResponseDTO.fromEntity(categoria);
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNome(dto.nome())) {
            throw new DuplicateResourceException("Já existe uma categoria cadastrada com o nome: " + dto.nome());
        }

        Categoria categoria = Categoria.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .build();

        return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listarTodas(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
                .map(CategoriaResponseDTO::fromEntity);
    }

    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada com o ID: " + id);
        }
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new DuplicateResourceException(
                "Não é possível excluir a categoria, pois existem produtos associados a ela."
            );
        }
        categoriaRepository.deleteById(id);
    }

    @Transactional
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID:"+id));
        if (categoriaRepository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new DuplicateResourceException("Já existe uma categoria cadastrada com o nome: " + dto.nome());
        }
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        return CategoriaResponseDTO.fromEntity(categoriaRepository.save(categoria));
    }

}