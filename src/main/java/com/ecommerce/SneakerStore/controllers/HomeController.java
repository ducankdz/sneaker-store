package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.dtos.*;
import com.ecommerce.SneakerStore.entities.Cart;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.entities.Wishlist;
import com.ecommerce.SneakerStore.responses.CartResponse;
import com.ecommerce.SneakerStore.responses.ProductResponse;
import com.ecommerce.SneakerStore.responses.WishlistResponse;
import com.ecommerce.SneakerStore.services.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final UserService userService;

    @GetMapping("")
    public String getProducts(Model model, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser!=null){
            List<Cart> carts = cartService.findCartByUserId(loggedInUser.getId());
            model.addAttribute("carts",carts);
        }
        String loginMessage = (String) session.getAttribute("message");
        session.removeAttribute("message");

        String message = (String) model.asMap().get("message");
        String errorMessage = (String) model.asMap().get("errorMessage");

        model.addAttribute("message",message);
        model.addAttribute("errorMessage",errorMessage);
        model.addAttribute("loginMessage",loginMessage);
        model.addAttribute("cartItemDTO",new CartItemDTO());
        model.addAttribute("newProducts",productService.newProducts().stream().map(ProductResponse::fromProduct).collect(Collectors.toList()));
        model.addAttribute("nikeProducts",productService.get8ProductsByCategory(1L).stream().map(ProductResponse::fromProduct).collect(Collectors.toList()));
        model.addAttribute("adidasProducts",productService.get8ProductsByCategory(2L).stream().map(ProductResponse::fromProduct).collect(Collectors.toList()));
        model.addAttribute("saleProducts",productService.getProductsWithHighestDiscount().stream().map(ProductResponse::fromProduct).collect(Collectors.toList()));
        return "index";
    }
    @GetMapping("/blog")
    public String directToBlog(){
        return "blog";
    }
    @GetMapping("/single-blog")
    public String directToSingleBlog(){
        return "single-blog";
    }

    private boolean isLoggedIn(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @GetMapping("/login")
    public String displayLoginPage(Model model,HttpSession session){
        if(isLoggedIn(session)){
            return "redirect:/";
        }
        model.addAttribute("loginDTO",new LoginDTO());
        String message = (String) model.asMap().get("message");
        String errorMessage = (String) model.asMap().get("errorMessage");
        return "login";
    }
    @GetMapping("/contact")
    public String displayContactPage(Model model){
        return "contact";
    }
    @GetMapping("/faq")
    public String displayFAQPage(Model model){
        return "faq";
    }
    @GetMapping("/wishlist")
    public String displayWishlistPage(
            @CookieValue("authToken") String token,
            Model model) throws Exception {
        List<WishlistResponse> wishlists = wishlistService.getWishListByUser(token);
        model.addAttribute("wishlists",wishlists);
        String message = (String) model.asMap().get("message");
        String errorMessage = (String) model.asMap().get("errorMessage");
        return "wishlist";
    }
    @GetMapping("/register")
    public String displayRegisterPage(Model model, HttpSession session){
        if(isLoggedIn(session)){
            return "redirect:/";
        }
        String message = (String) model.asMap().get("message");
        String errorMessage = (String) model.asMap().get("errorMessage");
        model.addAttribute("userDTO",new UserDTO());
        return "register";
    }
    @GetMapping("/cart")
    public String displayCartPage(Model model, HttpSession session) {
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        List<Cart> carts = cartService.findCartByUserId(loggedInUser.getId());
        List<CartResponse> cartResponses = carts.stream().map(CartResponse::fromCart).toList();
        model.addAttribute("carts",cartResponses);

        long subTotal = 0;
        for (Cart cart : carts) {
            long sellPrice = (long) (cart.getProduct().getPrice() * (1- cart.getProduct().getDiscount()));
            subTotal += cart.getQuantity() * sellPrice;
        }
        // Format subTotal
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        model.addAttribute("subTotal",decimalFormat.format(subTotal));
        String message = (String) model.asMap().get("message");
        String errorMessage = (String) model.asMap().get("errorMessage");
        model.addAttribute("message",message);
        model.addAttribute("errorMessage",errorMessage);
        return "cart";
    }
    @PostMapping("/checkout")
    public String displayCheckout(Model model,
                                  @RequestParam("shippingMethod") String shippingMethod,
                                  @CookieValue("authToken") String token) throws Exception {
        List<CartResponse> carts = cartService.getCartByUser(token);
        model.addAttribute("carts", carts);

        String strShippingMethod = switch (shippingMethod) {
            case "Tiêu Chuẩn" -> "30,000";
            case "Nhanh" -> "40,000";
            case "Hoả Tốc" -> "50,000";
            default -> "";
        };

        model.addAttribute("shippingMethod", shippingMethod);
        model.addAttribute("strShippingMethod", strShippingMethod);
        model.addAttribute("subtotal", cartService.subtotalCartMoney(token));
        model.addAttribute("total", cartService.totalCartMoney(shippingMethod, token));

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setShippingMethod(shippingMethod);
        orderDTO.setTotalMoney(cartService.totalCartMoney(shippingMethod, token));
        model.addAttribute("orderDTO", orderDTO);

        return "checkout";
    }
    @GetMapping("/404")
    public String display404(Model model){
        return "404";
    }

    @GetMapping("/order-history")
    public String displayOrderHistory(Model model){
        return "order-history";
    }

    @GetMapping("/account")
    public String displayAccount(Model model,
                                 @CookieValue("authToken") String token) throws Exception {
        User user = userService.getUserFromToken(token);
        model.addAttribute("user",user);
        model.addAttribute("userDTO",new UserDTO());
        model.addAttribute("changePasswordDTO",new ChangePasswordDTO());
        String message = (String) model.asMap().get("message");
        String errorMessage = (String) model.asMap().get("errorMessage");
        model.addAttribute("message",message);
        model.addAttribute("errorMessage",errorMessage);
        return "my-account";
    }

}
