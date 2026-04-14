package br.com.knowledge.stockonyou.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.request.SaleRequest;
import br.com.knowledge.stockonyou.service.SaleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody SaleRequest request) {
        saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
