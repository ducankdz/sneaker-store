package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.entities.Wishlist;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private Long id;
    private User user;
    private ProductResponse product;
    public static WishlistResponse fromWishList(Wishlist wishlist){
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .user(wishlist.getUser())
                .product(ProductResponse.fromProduct(wishlist.getProduct()))
                .build();
    }
}
