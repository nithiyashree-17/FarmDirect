package com.farmdirect.service;

import com.farmdirect.entity.*;
import com.farmdirect.repository.OrderRepository;
import com.farmdirect.repository.OrderTrackingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderTrackingEventRepository trackingRepo;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        OrderTrackingEventRepository trackingRepo,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.trackingRepo = trackingRepo;
        this.productService = productService;
    }

    @Transactional
    public Order createOrder(User customer, User farmer,
                             Map<Product, Double> cartItems,
                             String deliveryType, String deliveryAddress, String notes) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setFarmer(farmer);
        order.setDeliveryType(deliveryType);
        order.setDeliveryAddress(deliveryAddress);
        order.setNotes(notes);
        order.setOrderDate(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusDays(2));

        double total = 0;
        for (Map.Entry<Product, Double> entry : cartItems.entrySet()) {
            Product product = entry.getKey();
            double qty = entry.getValue();

            OrderItem item = new OrderItem(product, qty);
            order.addItem(item);
            total += item.getSubtotal();

            // Reduce stock
            product.setStockQuantity(product.getStockQuantity() - qty);
            if (product.getStockQuantity() <= 0) {
                product.setAvailable(false);
            }
            productService.save(product);
        }

        order.setTotalAmount(total);
        double deliveryCharge = "DELIVERY".equals(deliveryType) ? 40.0 : 0.0;
        order.setDeliveryCharge(deliveryCharge);
        order.setGrandTotal(total + deliveryCharge);
        order.setStatus(OrderStatus.PLACED);

        order.addTrackingEvent(OrderStatus.PLACED,
                "Order placed successfully. Waiting for payment.");

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus, String description) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(newStatus);
        order.addTrackingEvent(newStatus, description);

        if (newStatus == OrderStatus.DELIVERED) {
            order.setActualDelivery(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }

    public List<Order> findByCustomer(User customer) {
        return orderRepository.findByCustomerOrderByOrderDateDesc(customer);
    }

    public List<Order> findByFarmer(User farmer) {
        return orderRepository.findByFarmerOrderByOrderDateDesc(farmer);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public long countByFarmer(User farmer) {
        return orderRepository.countByFarmer(farmer);
    }

    public Double totalEarnings(User farmer) {
        Double total = orderRepository.totalEarningsByFarmer(farmer);
        return total != null ? total : 0.0;
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<OrderTrackingEvent> getTrackingEvents(Order order) {
        return trackingRepo.findByOrderOrderByTimestampAsc(order);
    }
}
