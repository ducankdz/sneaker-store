package com.ecommerce.SneakerStore.dtos;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangePasswordDTO {
    private String oldPassword;

    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự")
    private String password;

    private String retypePassword;
}
