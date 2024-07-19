package com.ecommerce.SneakerStore.global;

import com.ecommerce.SneakerStore.entities.Cart;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.CartRepository;
import com.ecommerce.SneakerStore.repositories.WishlistRepository;
import com.ecommerce.SneakerStore.responses.CartResponse;
import com.ecommerce.SneakerStore.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final CartRepository cartRepository;

    private final WishlistRepository wishlistRepository;

    public GlobalControllerAdvice(CartService cartService, CartRepository cartRepository, WishlistRepository wishlistRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
    }

    @ModelAttribute("crts")
    public List<CartResponse> carts(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser!=null){
            return cartService.findCartByUserId(loggedInUser.getId())
                    .stream()
                    .map(CartResponse ::fromCart)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @ModelAttribute("crtSize")
    public int cartSize(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser!=null){
            return cartService.findCartByUserId(loggedInUser.getId()).size();
        }
        return 0;
    }
    @ModelAttribute("sbTotal")
    public String subTotal(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser!=null){
            List<Cart> carts = cartRepository.findByUserId(loggedInUser.getId());
            long total = 0L;
            for(Cart cart : carts){
                long sellPrice = (long) (cart.getProduct().getPrice() * (1- cart.getProduct().getDiscount()));

                total += cart.getQuantity() * sellPrice;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            return decimalFormat.format(total);
        }
        return "";
    }
    @ModelAttribute("wishlstSize")
    public int wishListSize(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser!=null){
            return wishlistRepository.findByUser(loggedInUser).size();
        }
        return 0;
    }
    @ModelAttribute("currentUrl")
    public String currentUrl(HttpServletRequest request){
        return request.getRequestURI();
    }
}

