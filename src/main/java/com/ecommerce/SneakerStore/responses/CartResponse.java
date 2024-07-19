package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.Cart;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.text.DecimalFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;

    private ProductResponse product;

    private User user;

    private Long quantity;

    private Long size;

    private String total;
    public static CartResponse fromCart(Cart cart){
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        long sellPrice = (long) (cart.getProduct().getPrice() * (1- cart.getProduct().getDiscount()));
        return CartResponse
                .builder()
                .id(cart.getId())
                .product(ProductResponse.fromProduct(cart.getProduct()))
                .quantity(cart.getQuantity())
                .size(cart.getSize())
                .user(cart.getUser())
                .total(decimalFormat.format(cart.getQuantity() * sellPrice))
                .build();
    }
}
