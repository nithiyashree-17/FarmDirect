package com.farmdirect.repository;

import com.farmdirect.entity.Order;
import com.farmdirect.entity.OrderStatus;
import com.farmdirect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByOrderDateDesc(User customer);
    List<Order> findByFarmerOrderByOrderDateDesc(User farmer);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByFarmerAndStatus(User farmer, OrderStatus status);
    long countByFarmer(User farmer);
    long countByCustomer(User customer);

    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.farmer = :farmer AND o.status != 'CANCELLED'")
    Double totalEarningsByFarmer(User farmer);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatusValue(OrderStatus status);
}
