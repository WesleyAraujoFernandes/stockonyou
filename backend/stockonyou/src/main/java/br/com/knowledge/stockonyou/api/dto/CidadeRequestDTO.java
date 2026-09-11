package br.com.knowledge.stockonyou.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CidadeRequestDTO(
    @NotBlank(message = "O nome da cidade é obrigatório.") 
    @Size(max = 80, message = "O nome da cidade não pode exceder 80 caracteres.") 
    String nome,
    @NotNull(message = "O ID da UF.") 
    Long ufId
) {
}
