package br.com.knowledge.stockonyou.api.dto;

public record ClienteDTO(
    Long id,
    String nome,
    String email,
    String telefone
) {

}
