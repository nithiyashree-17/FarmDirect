package com.farmdirect.service;

import com.farmdirect.entity.Order;
import com.farmdirect.entity.OrderStatus;
import com.farmdirect.entity.Payment;
import com.farmdirect.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    public PaymentService(PaymentRepository paymentRepository, OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
    }

    /**
     * Mock payment — simulates Razorpay/GPay style payment
     * Generates a MOCKPAY-XXXXXXXX transaction ID
     * Always succeeds (for demo purposes)
     */
    @Transactional
    public Payment processPayment(Order order, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getGrandTotal());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId("MOCKPAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);

        // Update order status to PAID
        orderService.updateStatus(order.getId(), OrderStatus.PAID,
                "Payment of ₹" + String.format("%.2f", order.getGrandTotal())
                        + " received via " + paymentMethod
                        + ". Transaction ID: " + payment.getTransactionId());

        return payment;
    }

    public Optional<Payment> findByOrder(Order order) {
        return paymentRepository.findByOrder(order);
    }
}
