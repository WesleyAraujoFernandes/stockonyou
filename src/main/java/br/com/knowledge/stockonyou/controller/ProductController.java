package br.com.knowledge.stockonyou.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.ProductDTO;
import br.com.knowledge.stockonyou.mapper.ProductMapper;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.findAll()
                .stream()
                .map(ProductMapper.INSTANCE::toDTO)
                .toList();
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        Product product = ProductMapper.INSTANCE.toEntity(productDTO);
        Product saved = productService.save(product);
        return ProductMapper.INSTANCE.toDTO(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        Product existing = productService.findById(id);

        existing.setName(productDTO.getName());
        existing.setCategory(productDTO.getCategory());
        existing.setUnit(productDTO.getUnit());
        existing.setPurchasePrice(productDTO.getPurchasePrice());
        existing.setSalePrice(productDTO.getSalePrice());
        existing.setStockQuantity(productDTO.getStockQuantity());
        existing.setMinimumStock(productDTO.getMinimumStock());

        Product updated = productService.save(existing);
        return ProductMapper.INSTANCE.toDTO(updated);
    }

}
