package br.com.knowledge.stockonyou.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.knowledge.stockonyou.dto.TopProductDTO;
import br.com.knowledge.stockonyou.model.CommandItem;

public interface CommandItemRepository extends JpaRepository<CommandItem, Long> {
    @Query("SELECT COALESCE(SUM(i.totalPrice),0) FROM CommandItem i WHERE i.command.id = :commandId")
    BigDecimal sumTotalByCommandId(Long commandId);

    @Query("""
                SELECT new br.com.knowledge.stockonyou.dto.TopProductDTO(
                    p.id,
                    p.name,
                    SUM(ci.quantity),
                    SUM(ci.subtotal)
                )
                FROM CommandItem ci
                JOIN ci.command c
                JOIN ci.product p
                WHERE c.status = 'PAID'
                AND c.paidAt >= :start
                AND c.paidAt < :end
                GROUP BY p.id, p.name
                ORDER BY SUM(ci.quantity) DESC
            """)
    List<TopProductDTO> findTopProductsToday(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
