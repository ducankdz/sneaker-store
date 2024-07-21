package com.ecommerce.SneakerStore.services;


import com.ecommerce.SneakerStore.entities.Role;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.RoleRepository;
import com.ecommerce.SneakerStore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository; // Thay thế UserRepository bằng interface hoặc class để tương tác với database
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Lấy thông tin người dùng từ OAuth2 provider
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Xử lý thông tin người dùng
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Kiểm tra xem người dùng đã tồn tại trong database chưa
        User user = userRepository.findByPhoneNumber(email).orElse(null);
        if (user == null) {
            // Nếu người dùng chưa tồn tại, tạo mới và lưu vào database
            Role role = roleRepository.findById(1L).orElse(null); // Đảm bảo role của người dùng được cấu hình đúng
            user = new User();
            user.setName(name);
            user.setPhoneNumber(email);
            user.setPassword(passwordEncoder.encode("")); // Có thể để trống hoặc đặt mặc định tùy vào yêu cầu
            user.setActive(true);
            user.setRole(role); // Thiết lập role cho người dùng
            userRepository.save(user);
        }

        // Tạo đối tượng OAuth2User để trả về
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email");
    }
}
