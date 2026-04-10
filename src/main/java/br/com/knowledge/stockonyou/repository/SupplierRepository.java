package br.com.knowledge.stockonyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

}
