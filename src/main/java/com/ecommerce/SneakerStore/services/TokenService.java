package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.entities.Token;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.TokenRepository;
import com.ecommerce.SneakerStore.utils.JwtTokenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private static final int MAX_TOKENS = 3;
    @Value("${jwt.expiration}")
    private int expiration; //save to an environment variable

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    private final TokenRepository tokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public Token addToken(User user, String token){
        List<Token> tokens = tokenRepository.findByUser(user);
        int tokenCount = tokens.size();
        //Số lượng token vượt quá giới hạn, xoá 1 token cũ
        if(tokenCount >= MAX_TOKENS){
            //Xoá token đầu tiên trong ds
            tokenRepository.delete(tokens.get(0));
        }

        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        Token newToken = Token.builder()
                .token(token)
                .user(user)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .refreshToken(UUID.randomUUID().toString())
                .refreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken))
                .build();

        return tokenRepository.save(newToken);
    }

    @Transactional
    public Token refreshToken( User user, String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if(existingToken == null){
            throw new Exception("Refresh Token does not exist");
        }
        if(existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())){
            tokenRepository.delete(existingToken);
            throw new Exception("Refresh Token is expired");
        }
        String newToken = jwtTokenUtil.generateToken(user);
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);

        existingToken.setToken(newToken);
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setRefreshToken(UUID.randomUUID().toString());
        existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));

        return tokenRepository.save(existingToken);
    }

    public User getUserByRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if (existingToken == null) {
            throw new Exception("Refresh Token does not exist");
        }
        if (existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(existingToken);
            throw new Exception("Refresh Token is expired");
        }
        return existingToken.getUser();
    }
}