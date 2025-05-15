package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    String name,

    @Email(message = "Tên đăng nhập có định dạng email không hợp lệ")
    String email,

    String password,
    String confirmPassword,

    String role,

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Số điện thoại không hợp lệ")
    @Size(min = 10, max = 15, message = "Số điện thoại phải có từ 10 đến 15 ký tự")
    String phoneNumber
) {}