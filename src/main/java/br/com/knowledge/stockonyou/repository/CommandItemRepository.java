package br.com.knowledge.stockonyou.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.knowledge.stockonyou.model.CommandItem;

public interface CommandItemRepository extends JpaRepository<CommandItem, Long> {
    @Query("SELECT COALESCE(SUM(i.totalPrice),0) FROM CommandItem i WHERE i.command.id = :commandId")
    BigDecimal sumTotalByCommandId(Long commandId);
}
