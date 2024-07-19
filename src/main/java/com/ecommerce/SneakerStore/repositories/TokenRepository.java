package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Token;
import com.ecommerce.SneakerStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token,Long> {
    List<Token> findByUser(User user);
    Token findByToken(String token);
    Token findByRefreshToken(String token);
}
