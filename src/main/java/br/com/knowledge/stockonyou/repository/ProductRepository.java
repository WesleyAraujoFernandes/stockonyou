package br.com.knowledge.stockonyou.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import br.com.knowledge.stockonyou.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByStockQuantityAsc();

    List<Product> findAllByOrderByStockQuantityDesc();

    @NonNull
    List<Product> findAll(@NonNull Sort sort);

    List<Product> findByStockQuantityLessThanEqualOrderByStockQuantityAsc(BigDecimal quantity);

    List<Product> findByStockQuantityLessThanEqualOrderByStockQuantityDesc(BigDecimal quantity);

    @Query("""
            SELECT p
            FROM Product p
            WHERE P.minimumStock IS NOT NULL,
            AND p.stockQuantity <= p.minimumStock
            AND p.stockQuantity >= 0
            """)
    List<Product> findCriticalStockProducts();
}
