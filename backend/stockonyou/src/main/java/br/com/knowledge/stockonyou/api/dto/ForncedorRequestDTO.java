package br.com.knowledge.stockonyou.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForncedorRequestDTO(
    @NotBlank(message = "O nome do fornecedor é obrigatório.")
    @Size(max = 120, message = "O nome do fornecedor não pode exceder 120 caracteres.")
    String nome,
    @Size(max = 255, message = "O email não pode exceder 255 caracteres.")
    String email,
    @Size(max = 20, message = "O telefone não pode exceder 20 caracteres.")
    String telefone,
    @Size(max = 18, message = "O CNPJ não pode exceder 18 caracteres.")
    String cnpj,
    @Size(max = 255, message = "O endereco não pode exceder 255 caracteres.")
    String endereco,
    @Size(max = 9, message = "O cep não pode exceder 9 caracteres.")
    String cep,
    @Size(max = 500, message = "A observação não pode exceder 500 caracteres.")
    String observacao,
    @NotBlank(message = "O ID da cidade é obrigatório.")
    Long cidadeId
) {}
