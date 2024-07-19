package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist,Long> {
    boolean existsByUserAndProduct(User user, Product product);
    List<Wishlist> findByUser(User user);
}
