package com.farmdirect.repository;

import com.farmdirect.entity.Favorite;
import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByCustomer(User customer);
    Optional<Favorite> findByCustomerAndProduct(User customer, Product product);
    boolean existsByCustomerAndProduct(User customer, Product product);
    void deleteByCustomerAndProduct(User customer, Product product);
}
