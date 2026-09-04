package br.com.knowledge.stockonyou.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.knowledge.stockonyou.api.model.StatusVenda;
import br.com.knowledge.stockonyou.api.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {
    Optional<Venda> findByClienteIdAndStatus(Long clienteId, StatusVenda status);
    List<Venda> findByStatus(StatusVenda status);
}
