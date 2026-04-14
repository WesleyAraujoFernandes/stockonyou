package br.com.knowledge.stockonyou.dto.request;

import java.util.List;

public record SaleRequest(List<SaleItemRequest> items) {

}
