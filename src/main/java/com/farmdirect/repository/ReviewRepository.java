package com.farmdirect.repository;

import com.farmdirect.entity.Product;
import com.farmdirect.entity.Review;
import com.farmdirect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByCustomer(User customer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double averageRatingByProduct(Product product);
}
