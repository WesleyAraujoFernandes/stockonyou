package br.com.knowledge.stockonyou.service;

import br.com.knowledge.stockonyou.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.OpenCommandRequest;
import br.com.knowledge.stockonyou.exception.CommandClosedException;
import br.com.knowledge.stockonyou.exception.CommandItemNotFoundExeption;
import br.com.knowledge.stockonyou.exception.CommandNotFoundException;
import br.com.knowledge.stockonyou.exception.InsufficientStockException;
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

    public Command openCommand(OpenCommandRequest request) {
        Command command = new Command();
        command.setCustomerName(request.customerName());
        command.setStatus(CommandStatus.OPEN);
        command.setOpenedAt(LocalDateTime.now());
        return commandRepository.save(command);
    }

    public Command findById(Long id) {
        return commandRepository.findById(id)
                .orElseThrow(() -> new CommandNotFoundException("Command not found with id " + id));
    }

    public List<Command> findByStatus(CommandStatus status) {
        return commandRepository.findByStatus(status);
    }

    public CommandItem addItem(Long commandId, Long productId, Integer quantity) {

        Command command = findOpenCommand(commandId);

        Product product = productService.findById(productId);

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        CommandItem item = new CommandItem();
        item.setCommand(command);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getSalePrice());

        item.setTotalPrice(
                product.getSalePrice()
                        .multiply(BigDecimal.valueOf(quantity)));

        item.setAddedAt(LocalDateTime.now());

        return itemRepository.save(item);
    }

    public void removeItemFromCommand(Long commandId, Long itemId) {
        CommandItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CommandItemNotFoundExeption("Item not found with id " + itemId));
        findOpenCommand(item.getCommand().getId());
        Product product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        productRepository.save(product);
        itemRepository.delete(item);
    }

    public Command closeCommand(Long id) {
        Command command = findOpenCommand(id);
        if (command == null || command.getStatus() != CommandStatus.OPEN) {
            throw new CommandClosedException("Command is not open");
        }
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
                    .orElseThrow(() -> new CommandNotFoundException("Command not found with id " + id));
            if (command.getStatus() != CommandStatus.OPEN) {
                throw new CommandClosedException(id);
            }
            return command;
        } else {
            throw new IllegalArgumentException("Command id is null");
        }
    }
}
