package br.com.knowledge.stockonyou.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.response.DailySummaryResponse;
import br.com.knowledge.stockonyou.service.CommandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final CommandService commandService;

    @GetMapping("/daily-summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary() {
        return ResponseEntity.ok(commandService.getDailySummary());
    }
}