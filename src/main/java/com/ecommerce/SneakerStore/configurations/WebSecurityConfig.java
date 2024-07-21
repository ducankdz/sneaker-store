package com.ecommerce.SneakerStore.configurations;

import com.ecommerce.SneakerStore.entities.Role;
import com.ecommerce.SneakerStore.filters.JwtTokenFilter;
import com.ecommerce.SneakerStore.services.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final OAuthAuthenicationSuccessHandler handler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> {
                    requests
                            .requestMatchers(
                                    "/register",
                                    "/login**",
                                    "/assets/**",
                                    "/css/**",
                                    "/js/**",
                                    "/images/**",
                                    "/webjars/**",
                                    "/uploads/**",
                                    "/favicon.ico").permitAll()

                            .requestMatchers(POST, "/users/register").permitAll()
                            .requestMatchers(POST, "/users/login").permitAll()
                            .requestMatchers(GET, "/sso/loginSuccess**").permitAll()
                            .requestMatchers(GET, "/oauth2/authorization/google**").permitAll()
                            .requestMatchers(GET, "/users/logout").permitAll()
                            .requestMatchers(GET, "/cart").permitAll()
                            .requestMatchers(GET, "/contact").permitAll()
                            .requestMatchers(GET, "/faq").permitAll()
                            .requestMatchers(GET, "/wishlist").permitAll()
                            .requestMatchers(GET, "/products").permitAll()
                            .requestMatchers(GET, "/").permitAll()
                            .requestMatchers(GET, "/404").permitAll()
                            .requestMatchers(GET, "/products**").permitAll()
                            .requestMatchers(GET, "/products/**").permitAll()
                            .requestMatchers(GET, "/categories**").permitAll()
                            .requestMatchers(GET, "/blog**").permitAll()
                            .requestMatchers(GET, "/single-blog**").permitAll()
                            .requestMatchers(GET, "/coupon/calculate**").permitAll()
                            .requestMatchers(GET, "/order-history**").hasRole(Role.USER)
                            .requestMatchers(POST, "/users/cart").hasRole(Role.USER)
                            .requestMatchers(POST, "/users/cart/all/delete**").hasRole(Role.USER)
                            .requestMatchers(GET, "/users/wishlist/add/**").hasRole(Role.USER)
                            .requestMatchers(GET, "/users/wishlist/delete/**").hasRole(Role.USER)
                            .requestMatchers(GET, "/users/cart/delete/**").hasRole(Role.USER)
                            .requestMatchers(POST, "/users/cart/update/**").hasRole(Role.USER)
                            .requestMatchers(POST, "/checkout").hasRole(Role.USER)
                            .requestMatchers(POST, "/orders/create").hasRole(Role.USER)
                            .requestMatchers(GET, "/orders/user/**").hasRole(Role.USER)
                            .requestMatchers(GET, "/orders/**").hasRole(Role.USER)
                            .requestMatchers(GET, "/orders/rebuy/**").hasRole(Role.USER)
                            .requestMatchers(GET, "/account").hasRole(Role.USER)
                            .requestMatchers(POST, "/users/update/**").hasRole(Role.USER)
                            .requestMatchers(POST, "/users/comment/add").hasRole(Role.USER)
                            .requestMatchers(GET, "/users/reaction/add").hasRole(Role.USER)
                            .requestMatchers(GET, "/payment/vnpay**").hasRole(Role.USER)
                            .requestMatchers(GET, "/payment/paypal**").hasRole(Role.USER)
                            .requestMatchers(GET, "/admin").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/category").hasRole(Role.ADMIN)
                            .requestMatchers(POST, "/admin/category/add").hasRole(Role.ADMIN)
                            .requestMatchers(POST, "/admin/category/update/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/category/delete/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/product").hasRole(Role.ADMIN)
                            .requestMatchers(POST, "/admin/product/add").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/user").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/user/role/update/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/user/lock/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/user/unlock/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/order").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/order/update/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/coupon").hasRole(Role.ADMIN)
                            .requestMatchers(POST, "/admin/coupon/add").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/coupon/update/**").hasRole(Role.ADMIN)
                            .requestMatchers(GET, "/admin/coupon/delete/**").hasRole(Role.ADMIN)
                            .requestMatchers(PathRequest.toStaticResources()
                                    .atCommonLocations()).permitAll()
                            .anyRequest().authenticated();
                })
                .formLogin(httpSecurityFormLoginConfigurer ->
                        httpSecurityFormLoginConfigurer.loginPage("/login").permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(handler)
                )

                .csrf(AbstractHttpConfigurer::disable);

        http.cors(cors -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of("*"));
            configuration.applyPermitDefaultValues();
            configuration.addAllowedOrigin("*");
            configuration.addAllowedHeader("*");
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
            configuration.setExposedHeaders(List.of("authToken", "refreshToken"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            cors.configurationSource(source);
        });

        return http.build();
    }

}
