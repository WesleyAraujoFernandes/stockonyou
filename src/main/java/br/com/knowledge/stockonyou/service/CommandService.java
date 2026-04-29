package br.com.knowledge.stockonyou.service;

import br.com.knowledge.stockonyou.repository.ProductRepository;
import br.com.knowledge.stockonyou.util.MoneyUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import br.com.knowledge.stockonyou.dto.DailyCashClosingDTO;
import br.com.knowledge.stockonyou.dto.TopProductDTO;
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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandService {
    private final ProductRepository productRepository;
    private final CommandRepository commandRepository;
    private final CommandItemRepository commandItemRepository;
    private final CommandItemRepository itemRepository;
    private final ProductService productService;

    public List<Command> findAll() {
        return commandRepository.findAll();
    }

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
            item.setUnitPrice(MoneyUtils.normalize(product.getSalePrice()));
            item.setTotalPrice(
                    MoneyUtils.normalize(product.getSalePrice()
                            .multiply(BigDecimal.valueOf(quantity))));
            item.setAddedAt(LocalDateTime.now());
            command.getItems().add(item);
        }
        recalculateTotal(command);
        return commandRepository.save(command);
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

    /*
     * The command needs to be closed before it can be paid
     */
    @Transactional
    public Command payCommand(Long id, PaymentMethod paymentMethod) {
        Command command = findOpenCommand(id);

        if (command.getStatus() != CommandStatus.CLOSED) {
            throw new CommandClosedException("Command is not closed");
        }

        for (CommandItem item : command.getItems()) {
            Product product = item.getProduct();
            Integer newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new InsufficientStockException(
                        "Insufficient stock for product " + product.getName());
            }
            product.setStockQuantity(newStock);
        }

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
            averageTicket = MoneyUtils
                    .normalize(totalSales.divide(BigDecimal.valueOf(totalCommands), 2, RoundingMode.HALF_UP));
        }
        return new DailySummaryResponse(totalSales, totalCommands, averageTicket);
    }

    public Command updateItemQuantity(Long commandId, Long itemId, Integer quantity) {
        CommandItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CommandItemNotFoundExeption("Item not found with id " + itemId));
        findOpenCommand(item.getCommand().getId());
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quantity == 0) {
            removeItemFromCommand(commandId, itemId);
        } else {
            item.setQuantity(quantity);
            item.setSubtotal(MoneyUtils.normalize(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity))));
            item.setTotalPrice(MoneyUtils.normalize(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity))));
            itemRepository.save(item);
        }
        recalculateTotal(item.getCommand());
        if (item.getCommand() == null) {
            throw new CommandNotFoundException("Command not found with id " + commandId);
        }
        return commandRepository.save(item.getCommand());
    }

    public DailyCashClosingDTO getDailyCashClosing() {
        List<Command> commands = commandRepository.findPaidToday(LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay());
        DailyCashClosingDTO dto = new DailyCashClosingDTO();
        BigDecimal total = MoneyUtils.normalize(MoneyUtils
                .normalize(commands.stream().map(Command::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)));
        dto.setTotalAmount(MoneyUtils.normalize(total));
        dto.setTotalCommands(commands.size());
        dto.setAverageTicket(MoneyUtils.normalize(commands.isEmpty() ? BigDecimal.ZERO
                : MoneyUtils.normalize(total.divide(BigDecimal.valueOf(commands.size()), 2, RoundingMode.HALF_UP))));
        dto.setCashTotal(MoneyUtils.normalize(sumByPayment(commands, PaymentMethod.CASH)));
        dto.setCreditCardTotal(MoneyUtils.normalize(sumByPayment(commands, PaymentMethod.CREDIT_CARD)));
        dto.setDebitCardTotal(MoneyUtils.normalize(sumByPayment(commands, PaymentMethod.DEBIT_CARD)));
        dto.setPixTotal(MoneyUtils.normalize(sumByPayment(commands, PaymentMethod.PIX)));
        dto.setTransferTotal(MoneyUtils.normalize(sumByPayment(commands, PaymentMethod.TRANSFER)));
        return dto;
    }

    private void recalculateTotal(Command command) {
        BigDecimal totalAmount = command.getItems().stream()
                .map(CommandItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        command.setTotalAmount(MoneyUtils.normalize(totalAmount));
    }

    private BigDecimal sumByPayment(List<Command> commands, PaymentMethod paymentMethod) {
        return MoneyUtils
                .normalize(commands.stream().filter(command -> command.getPaymentMethod().equals(paymentMethod))
                        .map(Command::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
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

    public List<Command> findPaidToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return commandRepository.findPaidToday(start, end);
    }

    public List<TopProductDTO> getTopProductsToday() {

        LocalDate today = LocalDate.now();

        return commandItemRepository.findTopProductsToday(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }

    public void deleteById(@NonNull Long id) {
        commandRepository.deleteById(id);
    }
}
