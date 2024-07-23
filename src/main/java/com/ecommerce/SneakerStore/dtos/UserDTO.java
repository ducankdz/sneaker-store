package com.ecommerce.SneakerStore.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotEmpty(message = "SĐT là bắt buộc")
    @Size(min = 10, max = 10,message = "SĐT phải có 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Mật khẩu nhập lại là bắt buộc")
    private String retypePassword;

    @NotEmpty(message = "Tên là bắt buộc")
    private String name;
}
