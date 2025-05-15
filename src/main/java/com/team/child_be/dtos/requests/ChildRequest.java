package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChildRequest {
    @NotBlank(message = "Tên hiển thị không được để trống")
    private String name;

    private String password;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Số điện thoại ")
    @Size(min = 10, max = 15, message = "Số điện thoại phải có từ  đến 15 ký tự")
    private String phoneNumber;

    private MultipartFile avatar;
}
