package com.farmdirect.repository;

import com.farmdirect.entity.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {
    List<DeliveryAgent> findByAvailableTrue();
    List<DeliveryAgent> findByArea(String area);
}
