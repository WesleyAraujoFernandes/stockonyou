package br.com.knowledge.stockonyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
