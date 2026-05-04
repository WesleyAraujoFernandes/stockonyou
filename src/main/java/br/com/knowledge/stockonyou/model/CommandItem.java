package br.com.knowledge.stockonyou.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "command_items")
@Data
public class CommandItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    private Command command;

    @ManyToOne
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "A quantidade é obrigatória")
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    @NotNull(message = "O preco unitário é obrigatório")
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    private LocalDateTime addedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
}