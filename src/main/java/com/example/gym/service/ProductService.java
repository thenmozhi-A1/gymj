package com.example.gym.service;

import com.example.gym.dto.ProductRequest;
import com.example.gym.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    List<ProductResponse> getLowStockProducts();
}
