package br.com.knowledge.stockonyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

}
