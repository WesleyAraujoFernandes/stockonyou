package br.com.knowledge.stockonyou.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.CommandItemRequest;
import br.com.knowledge.stockonyou.dto.request.SaleItemRequest;
import br.com.knowledge.stockonyou.dto.request.SaleRequest;
import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandItem;
import br.com.knowledge.stockonyou.model.CommandStatus;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.model.Sale;
import br.com.knowledge.stockonyou.model.SaleItem;
import br.com.knowledge.stockonyou.repository.CommandRepository;
import br.com.knowledge.stockonyou.repository.ProductRepository;
import br.com.knowledge.stockonyou.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final CommandRepository commandRepository;

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

    @Transactional
    public void addItem(Long commandId, CommandItemRequest request) {
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Command not found with id " + commandId));
        if (command.getStatus() != CommandStatus.OPEN) {
            throw new IllegalArgumentException("Command is not open");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id " + request.productId()));
        if (product.getStockQuantity() < request.quantity()) {
            throw new IllegalArgumentException("Insufficient stock for product " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - request.quantity());
        CommandItem item = new CommandItem();
        item.setCommand(command);
        item.setProduct(product);
        item.setQuantity(request.quantity());
        item.setUnitPrice(product.getSalePrice());
        item.setTotalPrice(product.getSalePrice() * request.quantity());
        item.setAddedAt(LocalDateTime.now());
        command.getItems().add(item);
        command.setTotalAmount(command.getTotalAmount() + item.getTotalPrice());
        commandRepository.save(command);
    }
}
