package br.com.knowledge.stockonyou.dto.request;

import br.com.knowledge.stockonyou.model.PaymentMethod;

public record PayCommandRequest(PaymentMethod paymentMethod) {

}
