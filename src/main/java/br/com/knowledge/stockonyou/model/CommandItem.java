package br.com.knowledge.stockonyou.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "command_items")
@Data
public class CommandItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Command command;

    @ManyToOne
    private Product product;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private LocalDateTime addedAt;
}