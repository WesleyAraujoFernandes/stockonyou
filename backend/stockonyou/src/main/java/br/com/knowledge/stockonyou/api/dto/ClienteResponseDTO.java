package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.Cliente;

public record ClienteResponseDTO(
    Long id,
    String nome,
    String email,
    String telefone
) {
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone());
    }
}
