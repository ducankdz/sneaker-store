package com.ecommerce.SneakerStore.filters;

import com.ecommerce.SneakerStore.entities.Token;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.services.TokenService;
import com.ecommerce.SneakerStore.services.UserService;
import com.ecommerce.SneakerStore.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final TokenService tokenService;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response); // enable bypass
                return;
            }

            // Lấy token từ cookie
            String token = getTokenFromCookies(request);
            String refreshToken = getRefreshTokenFromCookies(request);

            if (token != null) {
                String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
                if (phoneNumber != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User userDetails = (User) userDetailsService.loadUserByUsername(phoneNumber);
                    if (jwtTokenUtil.validateToken(token, userDetails)) {
                        setAuthentication(request, userDetails);
                    }
                }
            } else if (refreshToken != null) {
                User user = tokenService.getUserByRefreshToken(refreshToken);
                Token newToken = tokenService.refreshToken(user, refreshToken);

                // Lưu token mới vào cookie
                addTokenToCookies(response, "authToken", newToken.getToken(), 60 * 30);
                addTokenToCookies(response, "refreshToken", newToken.getRefreshToken(), 30 * 24 * 60 * 60);

                setAuthentication(request, user);
            }

            filterChain.doFilter(request, response); // enable bypass
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
    private void addTokenToCookies(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    private void setAuthentication(HttpServletRequest request, User userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        List<Pair<String, String>> byPassTokens = Arrays.asList(
                Pair.of("/", "GET"),
                Pair.of("/404", "GET"),
                Pair.of("/categories", "GET"),
                Pair.of("/products", "GET"),
                Pair.of("/products/**", "GET"),
                Pair.of("/blog", "GET"),
                Pair.of("/single-blog", "GET"),
                Pair.of("/login**", "GET"),
                Pair.of("/sso/loginSuccess**", "GET"),
                Pair.of("/oauth2/authorization/google**", "GET"),
                Pair.of("/register", "GET"),
                Pair.of("/users/register", "POST"),
                Pair.of("/users/login", "POST"),
                Pair.of("/users/logout", "GET"),
                Pair.of("/cart", "GET"),
                Pair.of("/contact", "GET"),
                Pair.of("/faq", "GET"),
                Pair.of("/wishlist", "GET"),
                Pair.of("/assets/**", "GET"), // Bỏ qua xác thực cho tài nguyên tĩnh
                Pair.of("/css/**", "GET"),
                Pair.of("/js/**", "GET"),
                Pair.of("/images/**", "GET"),
                Pair.of("/webjars/**", "GET"),
                Pair.of("/uploads/**", "GET"),
                Pair.of("/coupon/calculate**", "GET")
        );
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        for (Pair<String, String> byPassToken : byPassTokens) {
            if (requestPath.matches(byPassToken.getFirst().replace("**", ".*"))
                    && requestMethod.equals(byPassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }

}
