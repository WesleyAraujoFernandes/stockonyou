package br.com.knowledge.stockonyou.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.api.model.Cidade;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {
    
}
