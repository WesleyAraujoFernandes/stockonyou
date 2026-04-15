package br.com.knowledge.stockonyou.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandStatus;

public interface CommandRepository extends JpaRepository<Command, Long> {
    List<Command> findByStatus(CommandStatus status);

    @Query("""
                SELECT
                    COALESCE(SUM(c.totalAmount), 0),
                    COUNT(c)
                FROM Command c
                WHERE c.status = :status
                AND c.paidAt BETWEEN :start AND :end
            """)
    Object[] getDailySummary(
            @Param("status") CommandStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
