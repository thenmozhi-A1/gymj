package com.example.gym.service;

import com.example.gym.dto.ProductDTO;
import com.example.gym.entity.Product;
import com.example.gym.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = new Product();
        mapToEntity(dto, product);
        product.setCreatedAt(Instant.now());
        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        mapToEntity(dto, product);
        product.setUpdatedAt(Instant.now());
        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(false);
        product.setUpdatedAt(Instant.now());
        productRepository.save(product);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());
        return dto;
    }

    private void mapToEntity(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setSku(dto.getSku());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }
    }
}
