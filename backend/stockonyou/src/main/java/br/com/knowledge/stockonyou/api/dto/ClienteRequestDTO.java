package br.com.knowledge.stockonyou.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
    @NotBlank(message = "O nome do cliente é obrigatório.")
    String nome,
    @Size(max = 255, message = "O email não pode exceder 255 caracteres.")
    String email,
    @Size(max = 20, message = "O telefone não pode exceder 20 caracteres.")
    String telefone
) {

}
