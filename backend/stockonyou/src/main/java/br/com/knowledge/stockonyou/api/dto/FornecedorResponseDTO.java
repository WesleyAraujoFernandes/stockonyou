package br.com.knowledge.stockonyou.api.dto;

import br.com.knowledge.stockonyou.api.model.Fornecedor;

public record FornecedorResponseDTO(
    Long id,
    String nome,
    String email,
    String telefone,
    String cnpj,
    String endereco,
    CidadeResponseDTO cidade,
    String cep,
    String observacao
) {
    public static FornecedorResponseDTO fromEntity(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(fornecedor.getId(),
            fornecedor.getNome(),
            fornecedor.getEmail(),
            fornecedor.getTelefone(),
            fornecedor.getCnpj(),
            fornecedor.getEndereco(),
            CidadeResponseDTO.fromEntity(fornecedor.getCidade()),
            fornecedor.getCep(),
            fornecedor.getObservacao());
    }
}
