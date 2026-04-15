package br.com.knowledge.stockonyou.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "commands")
@Data
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O nome do cliente é obrigatório")
    private String customerName;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    private CommandStatus status;

    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "command", cascade = CascadeType.ALL)
    private List<CommandItem> items;

    private PaymentMethod paymentMethod;

    private BigDecimal paidAmount;

    private LocalDateTime paidAt;
}