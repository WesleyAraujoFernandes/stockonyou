package br.com.knowledge.stockonyou.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.ProductRequest;
import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.mapper.ProductMapper;
import br.com.knowledge.stockonyou.model.Category;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.repository.CategoryRepository;
import br.com.knowledge.stockonyou.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @SuppressWarnings("null")
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @SuppressWarnings("null")
    public Product create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(
                () -> new ResourceNotFoundException("Category not found with id " + request.categoryId()));
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product existing = findById(id);
        if (existing == null || existing.getId() == null) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
        Product updated = productMapper.toEntity(request);
        updated.setId(existing.getId());
        return productRepository.save(updated);
    }

    @SuppressWarnings("null")
    public void delete(Long id) {
        Product product = findById(id);
        if (product.getId() != null) {
            productRepository.deleteById(product.getId());
        } else {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findAll(Sort.by("stockQuantity").ascending());
    }

    public List<Product> getHighStockProducts() {
        return productRepository.findAll(Sort.by("stockQuantity").descending());
    }

    public List<Product> getTopLowStockProducts(Integer quantity) {
        return productRepository.findByStockQuantityLessThanEqualOrderByStockQuantityAsc(BigDecimal.valueOf(quantity));
    }

    public List<Product> getTopHighStockProducts(Integer quantity) {
        return productRepository.findByStockQuantityLessThanEqualOrderByStockQuantityDesc(BigDecimal.valueOf(quantity));
    }

    public List<Product> getCriticalStockProducts() {
        return productRepository.findCriticalStockProducts();
    }
}