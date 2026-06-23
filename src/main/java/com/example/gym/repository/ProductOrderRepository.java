package com.example.gym.repository;

import com.example.gym.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByUserIdOrderByOrderDateDesc(Long userId);
    List<ProductOrder> findAllByOrderByOrderDateDesc();
}
