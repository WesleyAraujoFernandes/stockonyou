package br.com.knowledge.stockonyou.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.ProductRequest;
import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.mapper.ProductMapper;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product existing = findById(id);

        if (existing.getId() == null) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }

        Product updated = productMapper.toEntity(request);
        updated.setId(existing.getId());

        return productRepository.save(updated);
    }

    public void delete(Long id) {
        Product product = findById(id);
        if (product.getId() != null) {
            productRepository.deleteById(product.getId());
        } else {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
    }
}