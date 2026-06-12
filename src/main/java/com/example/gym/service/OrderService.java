package com.example.gym.service;

import com.example.gym.dto.OrderRequestDTO;
import com.example.gym.dto.OrderResponseDTO;
import com.example.gym.entity.Product;
import com.example.gym.entity.ProductOrder;
import com.example.gym.entity.User;
import com.example.gym.repository.ProductOrderRepository;
import com.example.gym.repository.ProductRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final ProductOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public OrderService(ProductOrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderResponseDTO placeOrder(String userEmail, OrderRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getIsActive() || product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Product not available or insufficient stock");
        }

        // Decrement stock
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        // Check low stock
        if (product.getStockQuantity() < 10) {
            notificationService.broadcast("LOW_STOCK", Map.of(
                    "productId", product.getId(),
                    "productName", product.getName(),
                    "stockLeft", product.getStockQuantity()
            ));
        }

        ProductOrder order = new ProductOrder();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setOrderStatus("CONFIRMED");
        order.setOrderDate(LocalDate.now());
        order.setCreatedAt(Instant.now());

        ProductOrder saved = orderRepository.save(order);
        return mapToDTO(saved);
    }

    public List<OrderResponseDTO> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserId(user.getId()).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, String status) {
        ProductOrder order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(status);
        order.setUpdatedAt(Instant.now());
        return mapToDTO(orderRepository.save(order));
    }

    private OrderResponseDTO mapToDTO(ProductOrder order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setProductId(order.getProduct().getId());
        dto.setProductName(order.getProduct().getName());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderDate(order.getOrderDate());
        return dto;
    }
}
