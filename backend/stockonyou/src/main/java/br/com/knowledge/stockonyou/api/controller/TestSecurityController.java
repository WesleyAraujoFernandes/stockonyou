package br.com.knowledge.stockonyou.api.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestSecurityController {
    @GetMapping("/public/ping")
    public ResponseEntity<Map<String, String>> publicEndPoint() {
        return ResponseEntity.ok(Map.of("message", "Endpoint público acessível sem token!"));
    }

    @GetMapping("/estoque/status")
    public ResponseEntity<Map<String, Object>> estoqueEndPoint(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "message", "Acesso permitido para o OPERADOR ou ADMIN",
                "usuario", jwt.getClaimAsString("preferred_username"),
                "email", jwt.getClaimAsString("email")));
    }

    @GetMapping("/admin/relatorios")
    public ResponseEntity<Map<String, Object>> adminEndPoint(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "message", "Usuário exclusivo de ADMIN",
                "usuario", jwt.getClaimAsString("preferred_username")));
    }
}
