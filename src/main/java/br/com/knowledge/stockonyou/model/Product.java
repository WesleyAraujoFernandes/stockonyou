package br.com.knowledge.stockonyou.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "O nome do produto é obrigatório")
    private String name;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @NotNull(message = "A unidade de medida é obrigatória")
    private String unit;
    @NotNull(message = "O preco de compra é obrigatório")
    private BigDecimal purchasePrice;
    @NotNull(message = "O preco de venda é obrigatório")
    private BigDecimal salePrice;
    @NotNull(message = "A quantidade em estoque é obrigatória")
    private Integer stockQuantity;
    @NotNull(message = "O estoque minimo é obrigatório")
    private Integer minimumStock;

}
