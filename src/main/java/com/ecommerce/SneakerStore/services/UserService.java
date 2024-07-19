package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.LoginDTO;
import com.ecommerce.SneakerStore.dtos.UserDTO;
import com.ecommerce.SneakerStore.entities.Role;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.RoleRepository;
import com.ecommerce.SneakerStore.repositories.UserRepository;
import com.ecommerce.SneakerStore.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public User register(UserDTO userDTO) throws Exception {
        if(userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())){
            throw new Exception("Phone number already exists");
        }
        if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
            throw new Exception("Password does not match");
        }
        Role role = roleRepository.findById(1L)
                .orElseThrow(() -> new Exception("Role does not exist"));
        User user = User.builder()
                .phoneNumber(userDTO.getPhoneNumber())
                .name(userDTO.getName())
                .isActive(true)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(role)
                .build();
        return userRepository.save(user);
    }
    public String login(LoginDTO loginDTO) throws Exception {
        User user = userRepository.findByPhoneNumber(loginDTO.getPhoneNumber())
                .orElseThrow(() -> new Exception("Wrong phone number or password"));
        if(!passwordEncoder.matches(loginDTO.getPassword(),user.getPassword())){
            throw new BadCredentialsException("Wrong phone number or password");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getPhoneNumber(),loginDTO.getPassword(),
                user.getAuthorities()
        );
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(user);
    }
    public String loginWithGoogle(OAuth2AuthenticationToken authenticationToken) throws Exception {
        String email = (String) authenticationToken.getPrincipal().getAttributes().get("email");
        String name = (String) authenticationToken.getPrincipal().getAttributes().get("name");
        Role role = roleRepository.findById(1L)
                .orElseThrow(() -> new Exception("Role does not exist"));
        if(userRepository.existsByPhoneNumber(email)){
            User existingUser = userRepository.findByPhoneNumber(email)
                    .orElseThrow(() -> new Exception("Wrong phone number or password"));
            return jwtTokenUtil.generateToken(existingUser);
        }
        User newUser = User.builder()
                .name(name)
                .phoneNumber(email)
                .password(passwordEncoder.encode(""))
                .isActive(true)
                .role(role)
                .build();
        userRepository.save(newUser);
        return jwtTokenUtil.generateToken(newUser);
    }
    public User getUserFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)){
            throw new Exception("Token is expired");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new Exception("Cannot find user"));
    }

    public User updateUser(UserDTO userDTO,String token,Long id) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User doesn't exist"));

        if(!user.getId().equals(getUserFromToken(token).getId())){
            throw new Exception("Cannot update another user");
        }

        if(userRepository.existByPhoneNumberForOtherUsers(phoneNumber,id)){
            throw new Exception("Phone number already exists");
        }

        user.setName(userDTO.getName());
        // Kiểm tra xem người dùng có cung cấp mật khẩu mới hay không
        if (!userDTO.getPassword().isEmpty()) {
            // Cập nhật mật khẩu nếu có mật khẩu mới từ DTO
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            // Giữ nguyên mật khẩu hiện tại nếu không có mật khẩu mới từ DTO
            user.setPassword(user.getPassword());
        }
        return userRepository.save(user);
    }
    public User updateRole(Long id, Long roleId) throws Exception {
        User existingUser = userRepository.findById(id).orElseThrow(() ->
                new Exception("Cannot find user with id = " + id));
        Role role = roleRepository.findById(roleId).orElseThrow(() ->
                new Exception("Cannot find role with id = " + roleId));
        existingUser.setRole(role);
        return userRepository.save(existingUser);
    }
    public User lockUser(Long id) throws Exception {
        User existingUser = userRepository.findById(id).orElseThrow(() ->
                new Exception("Cannot find user with id = " + id));
        existingUser.setActive(false);
        return userRepository.save(existingUser);
    }

    public User unLockUser(Long id) throws Exception {
        User existingUser = userRepository.findById(id).orElseThrow(() ->
                new Exception("Cannot find user with id = " + id));
        existingUser.setActive(true);
        return userRepository.save(existingUser);
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public Map<String, Object> getUserGrowthStatistics(){
        return Map.of(
                "months",List.of("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5",
                        "Tháng 6", "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"),
                "userCounts",List.of(userRepository.countUsersByMonth(1),
                        userRepository.countUsersByMonth(2),
                        userRepository.countUsersByMonth(3),
                        userRepository.countUsersByMonth(4),
                        userRepository.countUsersByMonth(5),
                        userRepository.countUsersByMonth(6),
                        userRepository.countUsersByMonth(7),
                        userRepository.countUsersByMonth(8),
                        userRepository.countUsersByMonth(9),
                        userRepository.countUsersByMonth(10),
                        userRepository.countUsersByMonth(11),
                        userRepository.countUsersByMonth(12)));
    }
}
