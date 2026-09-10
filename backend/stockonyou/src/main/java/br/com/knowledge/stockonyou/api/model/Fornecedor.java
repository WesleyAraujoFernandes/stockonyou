package br.com.knowledge.stockonyou.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fornecedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)  
    private String nome;
    @Column(length = 255)
    private String email;
    @Column(length = 20)
    private String telefone;
    @Column(length = 14)
    private String cnpj;
    @Column(length = 255)
    private String endereco;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidade_id")
    private Cidade cidadeId;
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uf_id")
    private UnidadeFederativa ufId;
     */
    @Column(length = 8)
    private String cep;  
}
