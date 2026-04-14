package br.com.knowledge.stockonyou.service;

import br.com.knowledge.stockonyou.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.exception.ResourceNotFoundException;
import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandItem;
import br.com.knowledge.stockonyou.model.CommandStatus;
import br.com.knowledge.stockonyou.model.Product;
import br.com.knowledge.stockonyou.repository.CommandItemRepository;
import br.com.knowledge.stockonyou.repository.CommandRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandService {
    private final ProductRepository productRepository;
    private final CommandRepository commandRepository;
    private final CommandItemRepository itemRepository;
    private final ProductService productService;

    public Command openCommand(String customerName) {
        Command command = new Command();
        command.setCustomerName(customerName);
        command.setStatus(CommandStatus.OPEN);
        command.setOpenedAt(LocalDateTime.now());
        return commandRepository.save(command);
    }

    public CommandItem addItem(Long commandId, Long productId, Integer quantity) {
        Command command = findOpenCommand(productId);
        Product product = productService.findById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        CommandItem item = new CommandItem();
        item.setCommand(command);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getSalePrice());
        item.setTotalPrice(product.getSalePrice() * quantity);
        item.setAddedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    public void removeItem(Long itemId) {
        CommandItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + itemId));
        Product product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        productRepository.save(product);
        itemRepository.deleteById(itemId);
    }

    public Command closeCommand(Long id) {
        Command command = findOpenCommand(id);
        command.setStatus(CommandStatus.CLOSED);
        command.setClosedAt(LocalDateTime.now());
        return commandRepository.save(command);
    }

    public BigDecimal calculateTotal(Long commandId) {
        return itemRepository.sumTotalByCommandId(commandId);
    }

    private Command findOpenCommand(Long id) {
        if (id != null) {
            Command command = commandRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Command not found with id " + id));
            if (command.getStatus() != CommandStatus.OPEN) {
                throw new IllegalArgumentException("Command is not open");
            }
            return command;
        } else {
            throw new IllegalArgumentException("Command id is null");
        }
    }
}
