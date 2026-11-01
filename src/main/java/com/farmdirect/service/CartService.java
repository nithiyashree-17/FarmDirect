package com.farmdirect.service;

import com.farmdirect.entity.Product;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * Session-scoped cart — each browser session has its own cart.
 * Cart is grouped by farmer so orders go to the right farmer.
 */
@Service
@VaadinSessionScope
public class CartService implements Serializable {

    // farmerId → (product → quantity)
    private final Map<Long, Map<Product, Double>> cart = new LinkedHashMap<>();

    public void addToCart(Product product, double quantity) {
        Long farmerId = product.getFarmer().getId();
        cart.computeIfAbsent(farmerId, k -> new LinkedHashMap<>());
        cart.get(farmerId).merge(product, quantity, Double::sum);
    }

    public void removeFromCart(Product product) {
        Long farmerId = product.getFarmer().getId();
        Map<Product, Double> farmerCart = cart.get(farmerId);
        if (farmerCart != null) {
            farmerCart.remove(product);
            if (farmerCart.isEmpty()) cart.remove(farmerId);
        }
    }

    public void updateQuantity(Product product, double quantity) {
        Long farmerId = product.getFarmer().getId();
        Map<Product, Double> farmerCart = cart.get(farmerId);
        if (farmerCart != null) {
            if (quantity <= 0) {
                farmerCart.remove(product);
                if (farmerCart.isEmpty()) cart.remove(farmerId);
            } else {
                farmerCart.put(product, quantity);
            }
        }
    }

    public Map<Long, Map<Product, Double>> getCart() {
        return Collections.unmodifiableMap(cart);
    }

    public Map<Product, Double> getItemsForFarmer(Long farmerId) {
        return cart.getOrDefault(farmerId, Collections.emptyMap());
    }

    public int getTotalItemCount() {
        return cart.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    public double getGrandTotal() {
        return cart.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .mapToDouble(e -> e.getKey().getPricePerUnit() * e.getValue())
                .sum();
    }

    public void clear() {
        cart.clear();
    }

    public void clearFarmerCart(Long farmerId) {
        cart.remove(farmerId);
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }
}
