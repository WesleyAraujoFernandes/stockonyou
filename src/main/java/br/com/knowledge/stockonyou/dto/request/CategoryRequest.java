package br.com.knowledge.stockonyou.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name) {

}
