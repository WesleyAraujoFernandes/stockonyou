package br.com.knowledge.stockonyou.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.request.StockMovementRequest;
import br.com.knowledge.stockonyou.service.StockMovementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService service;

    @PostMapping
    public ResponseEntity<Void> move(@RequestBody StockMovementRequest request) {
        service.move(request);
        return ResponseEntity.ok().build();
    }
}