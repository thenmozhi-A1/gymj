package com.example.gym.controller;

import com.example.gym.entity.Product;
import com.example.gym.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        List<Product> products = productRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("data", products);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Product product = productOpt.get();
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setSku(productDetails.getSku());
        product.setImageUrl(productDetails.getImageUrl());
        
        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        productRepository.delete(productOpt.get());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);
    }
}
