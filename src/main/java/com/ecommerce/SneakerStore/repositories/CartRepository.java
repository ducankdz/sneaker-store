package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Cart;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserAndProductAndSize(User user, Product product, Long size);
    void deleteByUserId(Long userId);
}