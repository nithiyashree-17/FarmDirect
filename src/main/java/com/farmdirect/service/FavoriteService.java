package com.farmdirect.service;

import com.farmdirect.entity.Favorite;
import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import com.farmdirect.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional
    public void toggleFavorite(User customer, Product product) {
        if (favoriteRepository.existsByCustomerAndProduct(customer, product)) {
            favoriteRepository.deleteByCustomerAndProduct(customer, product);
        } else {
            favoriteRepository.save(new Favorite(customer, product));
        }
    }

    public boolean isFavorite(User customer, Product product) {
        return favoriteRepository.existsByCustomerAndProduct(customer, product);
    }

    public List<Favorite> findByCustomer(User customer) {
        return favoriteRepository.findByCustomer(customer);
    }
}
