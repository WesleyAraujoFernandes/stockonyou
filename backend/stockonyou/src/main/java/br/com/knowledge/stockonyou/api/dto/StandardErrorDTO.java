package br.com.knowledge.stockonyou.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StandardErrorDTO(
                LocalDateTime timestamp,
                Integer status,
                String error,
                String message,
                String path,
                Map<String, String> fieldErrors) {
        public StandardErrorDTO(LocalDateTime timestamp, Integer status, String error, String message, String path) {
                this(timestamp, status, error, message, path, null);
        }
}