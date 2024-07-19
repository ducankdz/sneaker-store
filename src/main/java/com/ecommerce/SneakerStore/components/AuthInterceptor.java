package com.ecommerce.SneakerStore.components;

import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("loggedInUser") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("authToken")) {
                        String token = cookie.getValue();
                        try {
                            User user = userService.getUserFromToken(token);
                            session.setAttribute("loggedInUser", user);
                        } catch (Exception e) {
                            // Token không hợp lệ hoặc hết hạn, xóa cookie
                            cookie.setMaxAge(0);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }
}
