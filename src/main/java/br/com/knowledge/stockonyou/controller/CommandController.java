package br.com.knowledge.stockonyou.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.TopProductDTO;
import br.com.knowledge.stockonyou.dto.request.CommandItemRequest;
import br.com.knowledge.stockonyou.dto.request.OpenCommandRequest;
import br.com.knowledge.stockonyou.dto.request.PayCommandRequest;
import br.com.knowledge.stockonyou.dto.response.CommandResponse;
import br.com.knowledge.stockonyou.mapper.CommandMapper;
import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandStatus;
import br.com.knowledge.stockonyou.service.CommandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;
    private final CommandMapper commandMapper;

    @PostMapping("/open")
    public ResponseEntity<Command> openCommand(@RequestBody OpenCommandRequest request) {
        Command command = commandService.openCommand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(command);
    }

    @GetMapping
    public ResponseEntity<List<CommandResponse>> findAll() {
        List<CommandResponse> response = commandService.findAll().stream().map(commandMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Command> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commandService.findById(id));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Command> addItem(
            @PathVariable Long commandId,
            @RequestBody CommandItemRequest request) {

        Command item = commandService.addItem(
                commandId,
                request.productId(),
                request.quantity());

        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/items/{commandId}/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long commandId, @PathVariable Long itemId) {
        commandService.removeItemFromCommand(commandId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommand(@NonNull @PathVariable Long id) {
        commandService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Command> closeCommand(@PathVariable Long id) {
        return ResponseEntity.ok(commandService.closeCommand(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Command> payCommand(@PathVariable Long id, @RequestBody PayCommandRequest request) {
        return ResponseEntity.ok(commandService.payCommand(id, request.paymentMethod()));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Command>> findByStatus(@PathVariable CommandStatus status) {
        return ResponseEntity.ok(commandService.findByStatus(status));
    }

    public ResponseEntity<List<TopProductDTO>> getTopProductsToday() {
        return ResponseEntity.ok(commandService.getTopProductsToday());
    }

    @PatchMapping("/{id}/update-item-quantity/{itemId}/{quantity}")
    public ResponseEntity<Command> updateItemQuantity(@PathVariable Long id, @PathVariable Long itemId,
            @PathVariable Integer quantity) {
        return ResponseEntity.ok(commandService.updateItemQuantity(id, itemId, quantity));
    }
}
