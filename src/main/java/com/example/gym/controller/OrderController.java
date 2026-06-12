package com.example.gym.controller;

import com.example.gym.dto.OrderRequestDTO;
import com.example.gym.dto.OrderResponseDTO;
import com.example.gym.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // assuming username is email
    }

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody OrderRequestDTO request) {
        String email = getCurrentUserEmail();
        return ResponseEntity.ok(orderService.placeOrder(email, request));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        String email = getCurrentUserEmail();
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_OFFICE')")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
