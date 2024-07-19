package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.CartItemDTO;
import com.ecommerce.SneakerStore.entities.Cart;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.CartRepository;
import com.ecommerce.SneakerStore.repositories.ProductRepository;
import com.ecommerce.SneakerStore.responses.CartResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    public Cart addToCart(CartItemDTO cartItemDTO, String token) throws Exception {
        User user = userService.getUserFromToken(token);
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new Exception(
                        "Cannot find product with id = " + cartItemDTO.getProductId()));
        Optional<Cart> optionalCart = cartRepository
                .findByUserAndProductAndSize(user,product, cartItemDTO.getSize());
        if(optionalCart.isPresent()){
            Cart existingCart = optionalCart.get();
            existingCart.setQuantity(existingCart.getQuantity() + cartItemDTO.getQuantity());
            return cartRepository.save(existingCart);
        }
        Cart newCart = Cart.builder()
                .product(product)
                .user(user)
                .size(cartItemDTO.getSize())
                .quantity(cartItemDTO.getQuantity())
                .build();
        return cartRepository.save(newCart);
    }
    public List<Cart> findCartByUserId(Long userId){
        return cartRepository.findByUserId(userId);
    }

    public void deleteCartById(Long id,String token) throws Exception {
        User user = userService.getUserFromToken(token);
        Cart cart = cartRepository.findById(id)
                        .orElseThrow(() -> new Exception("Cannot find cart with id = " + id));
        if(!user.getId().equals(cart.getUser().getId())){
            throw new Exception("Cannot delete with other user");
        }
        cartRepository.deleteById(id);
    }
    public void updateQuantity(Long id, Long quantity) throws Exception {
        Cart existingCart = cartRepository.findById(id)
                .orElseThrow(() -> new Exception("Cannot find cart item with id = " + id));
        existingCart.setQuantity(quantity);
        cartRepository.save(existingCart);
    }
    public List<CartResponse> getCartByUser(String token) throws Exception {
        User user = userService.getUserFromToken(token);

        return cartRepository.findByUserId(user.getId())
                .stream()
                .map(CartResponse::fromCart)
                .collect(Collectors.toList());
    }
    public String subtotalCartMoney(String token) throws Exception {
        User user = userService.getUserFromToken(token);

        List<Cart> carts = cartRepository.findByUserId(user.getId());
        long total = 0L;
        for(Cart cart : carts){
            long sellPrice = (long) (cart.getProduct().getPrice() * (1- cart.getProduct().getDiscount()));
            total += cart.getQuantity() * sellPrice;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(total);
    }
    public String totalCartMoney(String shippingMethod,String token) throws Exception {
        User user = userService.getUserFromToken(token);

        List<Cart> carts = cartRepository.findByUserId(user.getId());
        long total = 0L;
        for(Cart cart : carts){
            long sellPrice = (long) (cart.getProduct().getPrice() * (1- cart.getProduct().getDiscount()));
            total += cart.getQuantity() * sellPrice;
        }
        long shippingCost = switch (shippingMethod) {
            case "Tiêu Chuẩn" -> 30000;
            case "Nhanh" -> 40000;
            case "Hoả Tốc" -> 50000;
            default -> 0;
        };

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(total+shippingCost);
    }
    public long convertStringToLong(String total){
        String str = total.replaceAll(",","");
        return Long.parseLong(str);
    }
    @Transactional
    public void deleteCartByUser(String token) throws Exception {
        User user = userService.getUserFromToken(token);
        cartRepository.deleteByUserId(user.getId());
    }

}
