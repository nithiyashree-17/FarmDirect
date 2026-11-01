package com.farmdirect.service;

import com.farmdirect.entity.DeliveryAgent;
import com.farmdirect.repository.DeliveryAgentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryAgentService {

    private final DeliveryAgentRepository repo;

    public DeliveryAgentService(DeliveryAgentRepository repo) {
        this.repo = repo;
    }

    public List<DeliveryAgent> findAll() {
        return repo.findAll();
    }

    public List<DeliveryAgent> findAvailable() {
        return repo.findByAvailableTrue();
    }

    public Optional<DeliveryAgent> findById(Long id) {
        return repo.findById(id);
    }

    public DeliveryAgent save(DeliveryAgent agent) {
        return repo.save(agent);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public DeliveryAgent assignAvailableAgent() {
        return repo.findByAvailableTrue().stream()
                .findFirst()
                .map(agent -> {
                    agent.setAvailable(false);
                    return repo.save(agent);
                })
                .orElse(null);
    }
}
