package br.com.knowledge.stockonyou.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.StockMovementRequest;
import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.model.MovementType;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.model.StockMovement;
import br.com.knowledge.stockonyou.repository.ProductRepository;
import br.com.knowledge.stockonyou.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final ProductRepository productRepository;
    private final StockMovementRepository repository;

    public void move(StockMovementRequest request) {

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.type() == MovementType.EXIT &&
                product.getStockQuantity() < request.quantity()) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        if (request.type() == MovementType.ENTRY) {
            product.setStockQuantity(
                    product.getStockQuantity() + request.quantity());
        } else {
            product.setStockQuantity(
                    product.getStockQuantity() - request.quantity());
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(request.type());
        movement.setQuantity(request.quantity());
        movement.setReason(request.reason());
        movement.setReference(request.reference());
        movement.setMovementDate(LocalDateTime.now());

        repository.save(movement);
        productRepository.save(product);
    }
}