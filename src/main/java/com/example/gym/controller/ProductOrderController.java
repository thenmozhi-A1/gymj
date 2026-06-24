package com.example.gym.controller;

import com.example.gym.dto.ProductOrderDTO;
import com.example.gym.entity.Product;
import com.example.gym.entity.ProductOrder;
import com.example.gym.entity.User;
import com.example.gym.repository.ProductOrderRepository;
import com.example.gym.repository.ProductRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class ProductOrderController {

    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductOrderController(ProductOrderRepository productOrderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.productOrderRepository = productOrderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private String currentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }
        if (principal instanceof User) {
            return ((User) principal).getEmail();
        }
        return principal.toString();
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            String email = currentUserEmail();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Long productId = Long.valueOf(payload.get("productId").toString());
            Integer quantity = Integer.valueOf(payload.getOrDefault("quantity", 1).toString());
            String shippingAddress = payload.getOrDefault("shippingAddress", "").toString();
            String paymentId = payload.getOrDefault("razorpayPaymentId", "").toString();

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product not found"));
            }

            if (product.getStockQuantity() < quantity) {
                return ResponseEntity.badRequest().body(Map.of("error", "Insufficient stock"));
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            ProductOrder order = new ProductOrder();
            order.setUser(user);
            order.setProduct(product);
            order.setQuantity(quantity);
            order.setUnitPrice(product.getPrice().doubleValue());
            order.setTotalPrice(product.getPrice().doubleValue() * quantity);
            order.setShippingAddress(shippingAddress);
            order.setRazorpayPaymentId(paymentId);
            order.setStatus("PAID");

            ProductOrder saved = productOrderRepository.save(order);
            return ResponseEntity.ok(mapToDTO(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<ProductOrderDTO> getAllOrders() {
        return productOrderRepository.findAllByOrderByOrderDateDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable("userId") Long userId) {
        try {
            String email = currentUserEmail();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || !user.getId().equals(userId)) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            List<ProductOrderDTO> orders = productOrderRepository.findByUserIdOrderByOrderDateDesc(userId)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return productOrderRepository.findById(id).map(order -> {
            String status = payload.get("status");
            if (status != null) {
                order.setStatus(status);
            }
            return ResponseEntity.ok(mapToDTO(productOrderRepository.save(order)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private ProductOrderDTO mapToDTO(ProductOrder order) {
        ProductOrderDTO dto = new ProductOrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getFullName());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setProductId(order.getProduct().getId());
        dto.setProductName(order.getProduct().getName());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setRazorpayPaymentId(order.getRazorpayPaymentId());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        return dto;
    }
}
