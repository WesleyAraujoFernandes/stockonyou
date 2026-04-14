package br.com.knowledge.stockonyou.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank String name,
        @NotBlank String contact,
        @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String cnpj) {

}
