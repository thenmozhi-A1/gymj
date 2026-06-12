package com.example.gym.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private Long productId;
    private Integer quantity;
}
