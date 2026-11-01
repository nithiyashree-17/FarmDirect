package com.farmdirect.repository;

import com.farmdirect.entity.Role;
import com.farmdirect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRole(Role role);
    List<User> findByRoleAndActiveTrue(Role role);
    long countByRole(Role role);
}
