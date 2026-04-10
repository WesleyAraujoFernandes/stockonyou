package br.com.knowledge.stockonyou.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.knowledge.stockonyou.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
