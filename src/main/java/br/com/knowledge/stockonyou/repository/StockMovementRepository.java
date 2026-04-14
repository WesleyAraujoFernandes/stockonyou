package br.com.knowledge.stockonyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

}
