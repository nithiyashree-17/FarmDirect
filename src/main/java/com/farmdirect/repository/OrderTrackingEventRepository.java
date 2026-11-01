package com.farmdirect.repository;

import com.farmdirect.entity.Order;
import com.farmdirect.entity.OrderTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderTrackingEventRepository extends JpaRepository<OrderTrackingEvent, Long> {
    List<OrderTrackingEvent> findByOrderOrderByTimestampAsc(Order order);
}
