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
@Table(name = "cidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uf_id", nullable = false)
    private UnidadeFederativa uf;
}
