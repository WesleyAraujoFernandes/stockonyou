package br.com.knowledge.stockonyou.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.request.CommandItemRequest;
import br.com.knowledge.stockonyou.dto.request.OpenCommandRequest;
import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandItem;
import br.com.knowledge.stockonyou.service.CommandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    @PostMapping("/open")
    public ResponseEntity<Command> openCommand(@RequestBody OpenCommandRequest request) {
        Command command = commandService.openCommand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(command);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Command> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commandService.findById(id));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CommandItem> addItem(
            @PathVariable Long id,
            @RequestBody CommandItemRequest request) {

        CommandItem item = commandService.addItem(
                id,
                request.productId(),
                request.quantity());

        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/items/{commandId}/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long commandId, @PathVariable Long itemId) {
        commandService.removeItemFromCommand(commandId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Command> closeCommand(@PathVariable Long id) {
        return ResponseEntity.ok(commandService.closeCommand(id));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> getTotal(@PathVariable Long id) {
        return ResponseEntity.ok(commandService.calculateTotal(id));
    }
}
