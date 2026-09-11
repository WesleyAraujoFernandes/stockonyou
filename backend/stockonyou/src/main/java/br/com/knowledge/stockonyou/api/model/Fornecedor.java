package br.com.knowledge.stockonyou.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
    @Column(length = 255, unique = true)
    private String email;
    @Column(length = 20)
    private String telefone;
    @Column(length = 18, unique = true)
    private String cnpj;
    @Column(length = 255)
    private String endereco;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cidade_id", nullable = false)
    private Cidade cidade;
    @Column(length = 9)
    private String cep;
    @Column(length = 500)
    private String observacao;
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

}
