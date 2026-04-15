package br.com.knowledge.stockonyou.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.Command;
import br.com.knowledge.stockonyou.model.CommandStatus;

public interface CommandRepository extends JpaRepository<Command, Long> {
    List<Command> findByStatus(CommandStatus status);
}
