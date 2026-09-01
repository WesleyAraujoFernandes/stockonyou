package br.com.knowledge.stockonyou.api.repository;

import br.com.knowledge.stockonyou.api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNome(String nome);
}