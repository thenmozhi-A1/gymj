package com.example.gym.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDate orderDate;
}
