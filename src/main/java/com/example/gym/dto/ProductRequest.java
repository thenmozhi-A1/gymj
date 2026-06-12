package com.example.gym.dto;

import com.example.gym.entity.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private ProductCategory category;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @NotBlank
    private String sku;

    private String imageUrl;
}
