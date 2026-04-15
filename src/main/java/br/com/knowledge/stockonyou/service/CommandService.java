package br.com.knowledge.stockonyou.service;

import br.com.knowledge.stockonyou.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.request.OpenCommandRequest;
import br.com.knowledge.stockonyou.dto.response.DailySummaryResponse;
import br.com.knowledge.stockonyou.exception.CommandClosedException;
import br.com.knowledge.stockonyou.exception.CommandItemNotFoundExeption;
import br.com.knowledge.stockonyou.exception.CommandNotFoundException;
import br.com.knowledge.stockonyou.exception.InsufficientStockException;
import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandItem;
import br.com.knowledge.stockonyou.model.CommandStatus;
import br.com.knowledge.stockonyou.model.PaymentMethod;
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

    public Command reopenCommand(Long id) {
        Command command = findOpenCommand(id);
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

    public Command addItem(Long commandId, Long productId, Integer quantity) {

        Command command = findOpenCommand(commandId);

        Product product = productService.findById(productId);

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Optional<CommandItem> existingItem = command.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if (existingItem.isPresent()) {
            CommandItem existing = existingItem.get();
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            CommandItem item = new CommandItem();
            item.setCommand(command);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getSalePrice());
            item.setTotalPrice(
                    product.getSalePrice()
                            .multiply(BigDecimal.valueOf(quantity)));
            item.setAddedAt(LocalDateTime.now());
            command.getItems().add(item);
        }
        recalculateTotal(command);
        return commandRepository.save(command);
    }

    private void recalculateTotal(Command command) {
        BigDecimal totalAmount = command.getItems().stream()
                .map(CommandItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        command.setTotalAmount(totalAmount);
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
        if (command.getStatus() != CommandStatus.OPEN && command.getStatus() != CommandStatus.PAID) {
            throw new CommandClosedException("Command is not open or paid");
        }
        command.setStatus(CommandStatus.CLOSED);
        command.setClosedAt(LocalDateTime.now());
        return commandRepository.save(command);
    }

    public Command payCommand(Long id, PaymentMethod paymentMethod) {
        Command command = findOpenCommand(id);

        command.setStatus(CommandStatus.PAID);
        command.setPaidAt(LocalDateTime.now());
        command.setPaymentMethod(paymentMethod);
        return commandRepository.save(command);
    }

    public DailySummaryResponse getDailySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        Object[] result = commandRepository.getDailySummary(CommandStatus.PAID, start, end);
        BigDecimal totalSales = (BigDecimal) result[0];
        Long totalCommands = (Long) result[1];
        BigDecimal averageTicket = BigDecimal.ZERO;
        if (totalCommands != null && totalCommands > 0) {
            averageTicket = totalSales.divide(BigDecimal.valueOf(totalCommands), 2, RoundingMode.HALF_UP);
        }
        return new DailySummaryResponse(totalSales, totalCommands, averageTicket);
    }

    private Command findOpenCommand(Long id) {
        if (id != null) {
            Command command = commandRepository.findById(id)
                    .orElseThrow(() -> new CommandNotFoundException("Command not found with id " + id));
            if (command.getStatus().equals(CommandStatus.CLOSED)) {
                throw new CommandClosedException("Command is closed and cannot be modified. Reopen the command:" + id
                        + " (" + command.getCustomerName() + ")");
            }
            if (command.getStatus().equals(CommandStatus.PAID)) {
                throw new CommandClosedException(
                        "Command is paid and cannot be modified. ID:" + id + " (" + command.getCustomerName() + ")");
            }
            if (command.getStatus().equals(CommandStatus.CANCELED)) {
                throw new CommandClosedException("Command is canceled and cannot be modified. ID:" + id + " ("
                        + command.getCustomerName() + ")");
            }
            return command;
        } else {
            throw new IllegalArgumentException("Command ID is null");
        }
    }
}
