package com.farmdirect.repository;

import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFarmer(User farmer);
    List<Product> findByFarmerAndAvailableTrue(User farmer);
    List<Product> findByAvailableTrue();
    List<Product> findByCategory(String category);
    List<Product> findByCategoryAndAvailableTrue(String category);
    List<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name);
    List<Product> findByOrganicTrueAndAvailableTrue();
    long countByFarmer(User farmer);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findAllCategories();

    @Query("SELECT p FROM Product p WHERE p.available = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(String keyword);
}
