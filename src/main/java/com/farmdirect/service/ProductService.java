package com.farmdirect.service;

import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import com.farmdirect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findByFarmer(User farmer) {
        return productRepository.findByFarmer(farmer);
    }

    public List<Product> findAvailableByFarmer(User farmer) {
        return productRepository.findByFarmerAndAvailableTrue(farmer);
    }

    public List<Product> findAllAvailable() {
        return productRepository.findByAvailableTrue();
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategoryAndAvailableTrue(category);
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAllAvailable();
        }
        return productRepository.searchProducts(keyword.trim());
    }

    public List<Product> findOrganic() {
        return productRepository.findByOrganicTrueAndAvailableTrue();
    }

    public List<String> findAllCategories() {
        return productRepository.findAllCategories();
    }

    public long countByFarmer(User farmer) {
        return productRepository.countByFarmer(farmer);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Find products from nearby farmers using Haversine distance
     */
    public List<Product> findNearbyProducts(double lat, double lon, double radiusKm) {
        return productRepository.findByAvailableTrue().stream()
                .filter(p -> {
                    double dist = haversineDistance(lat, lon,
                            p.getFarmer().getLatitude(), p.getFarmer().getLongitude());
                    return dist <= radiusKm;
                })
                .toList();
    }

    /**
     * Haversine distance in km between two lat/lon points
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
