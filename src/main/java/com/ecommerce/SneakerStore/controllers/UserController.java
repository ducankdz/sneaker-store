package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.dtos.*;
import com.ecommerce.SneakerStore.entities.*;
import com.ecommerce.SneakerStore.responses.CommentResponse;
import com.ecommerce.SneakerStore.responses.ProductResponse;
import com.ecommerce.SneakerStore.services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final TokenService tokenService;

    @PostMapping("/register")
    public String register(Model model,
                           @Valid @ModelAttribute("userDTO") UserDTO userDTO) throws Exception {
        User user = userService.register(userDTO);
        model.addAttribute("user",user);
        model.addAttribute("loginDTO",new LoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String login(RedirectAttributes redirectAttributes,
                        Model model,
                        @Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                        HttpServletResponse response,
                        HttpSession session) throws Exception {
        try {
            String token = userService.login(loginDTO);
            model.addAttribute("token",token);

            User user = userService.getUserFromToken(token);
            model.addAttribute("user",user);

            Token jwtToken = tokenService.addToken(user,token);

            if(!user.isActive()){
                model.addAttribute("errorMessage",
                        "Tài khoản của bạn đã bị khoá.");
                return "/login";
            }

            // Lưu token vào cookie
            Cookie authTokenCookie  = new Cookie("authToken", token);
            authTokenCookie.setHttpOnly(true);
            authTokenCookie.setMaxAge(60*30);
            authTokenCookie.setPath("/");
            response.addCookie(authTokenCookie);

            // Lưu refreshToken vào cookie
            Cookie refreshTokenCookie  = new Cookie("refreshToken", jwtToken.getRefreshToken());
            refreshTokenCookie .setHttpOnly(true);
            refreshTokenCookie .setMaxAge(30 * 24 * 60 * 60);
            refreshTokenCookie .setPath("/");
            response.addCookie(refreshTokenCookie );

            // Lưu thông tin người dùng vào session
            session.setAttribute("loggedInUser", user);
            session.setAttribute("message", "Đăng nhập thành công.");
            if(user.getRole().getId()==1){
                return "redirect:/";
            }
            return "redirect:/admin";
        }
        catch (Exception e){
            model.addAttribute("errorMessage", "Số điện thoại hoặc mật khẩu không chính xác.");
            return "/login";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate(); // Xóa toàn bộ session

        // Xóa authToken cookie
        Cookie authTokenCookie = new Cookie("authToken", null);
        authTokenCookie.setMaxAge(0);
        authTokenCookie.setPath("/");
        response.addCookie(authTokenCookie);

        // Xóa refreshToken cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return "redirect:/";
    }
    @PostMapping("/cart")
    public String addToCart(@CookieValue("authToken") String token,
                            @RequestParam("currentUrl") String currentUrl,
                            @Valid @ModelAttribute("cartItemDTO") CartItemDTO cartItemDTO,
                            RedirectAttributes redirectAttributes) throws Exception {
        try {
            Cart cart = cartService.addToCart(cartItemDTO, token);
            redirectAttributes.addFlashAttribute("message",
                    "Thêm vào giỏ hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    "Thêm vào giỏ hàng thất bại.");
        }
        return "redirect:" + currentUrl;
    }

    @PostMapping("/cart/all/delete")
    public String deleteUserCart(@CookieValue("authToken") String token,
                                 @RequestParam("currentUrl") String currentUrl,
                                 RedirectAttributes redirectAttributes) throws Exception {
        try {
            cartService.deleteCartByUser(token);
            redirectAttributes.addFlashAttribute("message",
                    "Xoá giỏ hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    "Xoá giỏ hàng thất bại.");
        }
        return "redirect:" + currentUrl;
    }


    @GetMapping("/wishlist/add/{productId}")
    public String addToWishlist(@CookieValue("authToken") String token,
                            @PathVariable("productId") Long productId,
                            RedirectAttributes redirectAttributes) throws Exception {
        try {
            Wishlist wishlist = wishlistService.addToWishList(productId,token);
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Thêm vào yêu thích thành công.");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Thêm vào yêu thích thất bại.");
        }
        return "redirect:/wishlist";
    }

    @GetMapping("wishlist/delete/{id}")
    public String deleteWishlistById(@PathVariable("id") Long id,
                                     @CookieValue("authToken") String token,
                                     RedirectAttributes redirectAttributes){
        try {
            wishlistService.deleteWishlistById(id,token);
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Xoá khỏi yêu thích thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Xoá khỏi yêu thích thất bại");
        }
        return "redirect:/wishlist";
    }

    @GetMapping("/cart/delete/{id}")
    public String deleteCartById(@PathVariable("id") Long id,
                                 @CookieValue("authToken") String token,
                                 RedirectAttributes redirectAttributes){
        try {
            cartService.deleteCartById(id,token);
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Xóa khỏi giỏ hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Xóa khỏi giỏ hàng thất bại.");
        }
        return "redirect:/cart"; // Chuyển hướng về trang giỏ hàng sau khi xóa
    }
    @PostMapping("/cart/update")
    public String updateCartQuantity(@RequestParam Map<String,String> quantities,
                                 RedirectAttributes redirectAttributes) throws Exception {
        try {
            for(Map.Entry<String,String> entry : quantities.entrySet()){
                String key = entry.getKey();
                String idStr = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
                Long id = Long.valueOf(idStr);

                Long quantity = Long.valueOf(entry.getValue());
                cartService.updateQuantity(id,quantity);
                redirectAttributes.addFlashAttribute(
                        "message",
                        "Cập nhật giỏ hàng thành công.");
            }
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Cập nhật giỏ hàng thất bại.");
        }

        return "redirect:/cart";
    }
    @PostMapping("/update/{id}")
    public String updateUser(@CookieValue("authToken") String token,
                             RedirectAttributes redirectAttributes,
                             @PathVariable("id") Long id,
                             @ModelAttribute("userDTO") UserDTO userDTO,
                             HttpSession session) throws Exception {
        String message;
        if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
            message = "Mật khẩu nhập lại không khớp";
        }
        else{
            User user = userService.updateUser(userDTO,token,id);
            message = "Cập nhật thông tin thành công";
            session.setAttribute("loggedInUser", user);
        }
        redirectAttributes.addFlashAttribute("message",message);
        return "redirect:/account";
    }
    @PostMapping("/comment/add")
    private String addComment(@CookieValue("authToken") String token,
                              @ModelAttribute("commentDTO")CommentDTO commentDTO,
                              RedirectAttributes redirectAttributes){
        try {
            Comment comment = commentService.addComment(commentDTO,token);
            redirectAttributes.addFlashAttribute("message","Đánh giá thành công.");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message","Đánh giá thất bại.");
        }
        return "redirect:/products/" + commentDTO.getProductId();
    }

    @GetMapping("/reaction/add/{commentId}")
    public String addReaction(@CookieValue("authToken") String token,
                              RedirectAttributes redirectAttributes,
                              @PathVariable("commentId") Long commentId,
                              @RequestParam("reactionType") String reactionType) throws Exception {
        CommentResponse comment = commentService.getCommentById(commentId);
        try {
            ReactionDTO reactionDTO = ReactionDTO.builder()
                    .commentId(commentId)
                    .reactionType(reactionType)
                    .build();
            reactionService.addReaction(reactionDTO,token);
            redirectAttributes.addFlashAttribute("message","Reaction thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message","Reaction thất bại");
        }
        return "redirect:/products/" + comment.getProduct().getId();
    }
}
