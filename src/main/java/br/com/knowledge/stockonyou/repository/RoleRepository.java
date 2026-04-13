package br.com.knowledge.stockonyou.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
