package br.com.knowledge.stockonyou.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(
        @NotBlank(message = "O nome da categoria é obrigatório.") @Size(max = 80, message = "O nome da categoria não pode exceder 80 caracteres.") String nome,

        @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.") String descricao) {
}