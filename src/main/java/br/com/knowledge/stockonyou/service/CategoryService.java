package br.com.knowledge.stockonyou.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.CategoryRequest;
import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.mapper.CategoryMapper;
import br.com.knowledge.stockonyou.model.Category;
import br.com.knowledge.stockonyou.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @SuppressWarnings("null")
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @SuppressWarnings("null")
    public Category create(CategoryRequest request) {
        return categoryRepository.save(categoryMapper.toEntity(request));
    }

    public Category update(Long id, CategoryRequest request) {
        Category existing = findById(id);
        if (existing == null || existing.getId() == null) {
            throw new ResourceNotFoundException("Category not found with id " + id);
        }
        Category updated = categoryMapper.toEntity(request);
        updated.setId(existing.getId()); // mantem o ID original
        return categoryRepository.save(updated);
    }

    @SuppressWarnings("null")
    public void delete(Long id) {
        Category category = findById(id);
        if (category.getId() != null) {
            categoryRepository.deleteById(category.getId());
        } else {
            throw new ResourceNotFoundException("Category not found with id " + id);
        }
    }
}
