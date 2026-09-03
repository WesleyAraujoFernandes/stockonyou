package br.com.knowledge.stockonyou.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.api.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
}
