package br.com.knowledge.stockonyou.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.knowledge.stockonyou.model.CommandStatus;
import br.com.knowledge.stockonyou.model.PaymentMethod;

public record CommandResponse(
        Long id,
        String customerName,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        LocalDateTime paidAt,
        CommandStatus status,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        PaymentMethod paymentMethod,
        List<CommandItemResponse> items) {

}
