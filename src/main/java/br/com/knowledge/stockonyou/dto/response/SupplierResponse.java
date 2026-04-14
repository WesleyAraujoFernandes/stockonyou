package br.com.knowledge.stockonyou.dto.response;

public record SupplierResponse(
        Long id,
        String name,
        String contact,
        String email,
        String phone,
        String cnpj) {

}
