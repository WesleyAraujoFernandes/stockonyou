package br.com.knowledge.stockonyou.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.SaleItemRequest;
import br.com.knowledge.stockonyou.dto.request.SaleRequest;
import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.model.Sale;
import br.com.knowledge.stockonyou.model.SaleItem;
import br.com.knowledge.stockonyou.repository.ProductRepository;
import br.com.knowledge.stockonyou.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public Sale createSale(SaleRequest request) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        List<SaleItem> items = new ArrayList<>();
        double total = 0.0;
        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id " + itemRequest.productId()));
            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for product " + product.getName());
            }
            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getSalePrice());

            double itemTotal = product.getSalePrice() * itemRequest.quantity();
            item.setTotalPrice(itemTotal);
            total += itemTotal;

            items.add(item);
            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());
            productRepository.save(product);
        }
        sale.setItems(items);
        sale.setTotalAmount(total);
        return saleRepository.save(sale);
    }

}
