package com.example.gym.repository;

import com.example.gym.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsActiveTrueOrderByCreatedAtDesc();
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    List<Product> findByIsActiveTrueAndStockQuantityLessThan(int threshold);
}
