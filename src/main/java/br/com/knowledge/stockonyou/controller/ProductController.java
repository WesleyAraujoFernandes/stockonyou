package br.com.knowledge.stockonyou.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.request.ProductRequest;
import br.com.knowledge.stockonyou.dto.response.ProductResponse;
import br.com.knowledge.stockonyou.mapper.ProductMapper;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper mapper;

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        Product product = productService.findById(id);
        return mapper.toResponse(product);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest productDTO) {
        Product saved = productService.create(productDTO);
        return mapper.toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest productDTO) {

        Product updated = productService.update(id, productDTO);
        return mapper.toResponse(updated);
    }

    public ResponseEntity<List<Product>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    public ResponseEntity<List<Product>> getHighStock() {
        return ResponseEntity.ok(productService.getHighStockProducts());
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<Product>> getLowStockAlerts() {
        return ResponseEntity.ok(productService.getCriticalStockProducts());
    }
}