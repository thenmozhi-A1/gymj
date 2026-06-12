package com.example.gym.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private String imageUrl;
    private Boolean isActive;
}
