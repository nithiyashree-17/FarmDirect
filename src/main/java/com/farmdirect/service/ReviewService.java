package com.farmdirect.service;

import com.farmdirect.entity.Product;
import com.farmdirect.entity.Review;
import com.farmdirect.entity.User;
import com.farmdirect.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    @Transactional
    public Review addReview(User customer, Product product,
                            com.farmdirect.entity.Order order,
                            int rating, String comment) {
        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);

        // Update product rating
        Double avgRating = reviewRepository.averageRatingByProduct(product);
        if (avgRating != null) {
            product.setRating(avgRating);
            product.setReviewCount(product.getReviewCount() + 1);
            productService.save(product);
        }
        return review;
    }

    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public List<Review> findByCustomer(User customer) {
        return reviewRepository.findByCustomer(customer);
    }
}
