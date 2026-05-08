package com.example.gym.service;

import com.example.gym.entity.Payment;
import com.example.gym.entity.User;
import com.example.gym.repository.PaymentRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    /** Save a new payment record for a user */
    public Payment addPayment(Long userId, Payment payment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        payment.setUser(user);
        return paymentRepository.save(payment);
    }

    /** Get all payments for a specific user */
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /** Get all payments (admin view) */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /** Get payment by ID */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    /** Update payment status (e.g. mark as SUCCESS) */
    public Payment updatePaymentStatus(Long id, String status) {
        Payment payment = getPaymentById(id);
        payment.setPaymentStatus(status);
        return paymentRepository.save(payment);
    }

    /** Delete payment record */
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
