package com.ecommerce.SneakerStore.configurations;

import com.ecommerce.SneakerStore.entities.Role;
import com.ecommerce.SneakerStore.entities.Token;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.RoleRepository;
import com.ecommerce.SneakerStore.repositories.UserRepository;
import com.ecommerce.SneakerStore.services.TokenService;
import com.ecommerce.SneakerStore.utils.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuthAuthenicationSuccessHandler implements AuthenticationSuccessHandler {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final TokenService tokenService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var oauth2AuthenicationToken = (OAuth2AuthenticationToken) authentication;

        String registrationId = oauth2AuthenicationToken
                .getAuthorizedClientRegistrationId();
        var oauthUser = (DefaultOAuth2User) authentication.getPrincipal();

        String name = null;
        String phoneNumber = null;

        if (registrationId.equals("facebook")) {
            // Facebook-specific attributes handling
            name = oauthUser.getAttribute("name");
            if (name == null) {
                String firstName = oauthUser.getAttribute("first_name");
                String lastName = oauthUser.getAttribute("last_name");
                name = firstName + " " + lastName;
            }
            phoneNumber = oauthUser.getAttribute("id");
        }
        else{
            name = oauthUser.getAttribute("name").toString();
            phoneNumber = oauthUser.getAttribute("email");
        }

        Role role = null;
        try {
            role = roleRepository.findById(1L)
                    .orElseThrow(() -> new Exception("Role does not exist"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        User newUser = User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .password(passwordEncoder.encode(""))
                .isActive(true)
                .role(role)
                .build();


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        User existingUser = userRepository.findByPhoneNumber(newUser.getPhoneNumber())
                .orElse(null);
        if (existingUser == null) {
            newUser = userRepository.save(newUser); // Save the new user before generating the token
        } else {
            newUser = existingUser;
        }
        String token = jwtTokenUtil.generateToken(newUser);
        Token jwtToken = tokenService.addToken(newUser,token);

        request.getSession().setAttribute("authToken",token);
        request.getSession().setAttribute("loggedInUser",newUser);

        // Lưu token vào cookie
        Cookie authTokenCookie  = new Cookie("authToken", token);
        authTokenCookie.setHttpOnly(true);
        authTokenCookie.setMaxAge(60 * 30); // Token tồn tại trong 1h
        authTokenCookie.setPath("/");
        response.addCookie(authTokenCookie);

        // Lưu refreshToken vào cookie
        Cookie refreshTokenCookie  = new Cookie("refreshToken", jwtToken.getRefreshToken());
        refreshTokenCookie .setHttpOnly(true);
        refreshTokenCookie .setMaxAge(30 * 24 * 60 * 60); // Refresh Token tồn tại trong 1 ngày
        refreshTokenCookie .setPath("/");
        response.addCookie(refreshTokenCookie );

        request.getSession().setAttribute("message","Đăng Nhập Thành Công.");

        new DefaultRedirectStrategy().sendRedirect(request, response, "/");
    }
}
