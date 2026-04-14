package br.com.knowledge.stockonyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.Command;

public interface CommandRepository extends JpaRepository<Command, Long> {

}
