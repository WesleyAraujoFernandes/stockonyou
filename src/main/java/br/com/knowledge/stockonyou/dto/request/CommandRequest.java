package br.com.knowledge.stockonyou.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record CommandRequest(
        Long supplierId,
        List<CommandItemRequest> items,
        LocalDateTime openedAt,
        LocalDateTime closedAt) {

}
