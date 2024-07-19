package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.WishlistItemDTO;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.entities.Wishlist;
import com.ecommerce.SneakerStore.repositories.ProductRepository;
import com.ecommerce.SneakerStore.repositories.WishlistRepository;
import com.ecommerce.SneakerStore.responses.WishlistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public Wishlist addToWishList(Long productId, String token) throws Exception {
        User user = userService.getUserFromToken(token);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception(
                        "Cannot find product with id = " + productId));
        if(wishlistRepository.existsByUserAndProduct(user,product)){
            return null;
        }

        Wishlist wishlist = Wishlist
                .builder()
                .user(user)
                .product(product)
                .build();
        return wishlistRepository.save(wishlist);
    }
    public List<WishlistResponse> getWishListByUser(String token) throws Exception {
        User user = userService.getUserFromToken(token);
        return wishlistRepository.findByUser(user)
                .stream()
                .map(WishlistResponse::fromWishList)
                .collect(Collectors.toList());
    }

    public void deleteWishlistById(Long id,String token) throws Exception {
        User user = userService.getUserFromToken(token);
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new Exception("Cannot find wishlist with id = " + id));
        if(!user.getId().equals(wishlist.getUser().getId())){
            throw new Exception("Cannot delete with other user");
        }
        wishlistRepository.deleteById(id);
    }
}
