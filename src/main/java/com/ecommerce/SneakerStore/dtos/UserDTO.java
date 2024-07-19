package com.ecommerce.SneakerStore.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotEmpty(message = "Phone number is required")
    private String phoneNumber;

    @NotEmpty(message = "Password is required")
    private String password;

    @NotEmpty(message = "Retype password is required")
    private String retypePassword;

    @NotEmpty(message = "Name is required")
    private String name;
}
