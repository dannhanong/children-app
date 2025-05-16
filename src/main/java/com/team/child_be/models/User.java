package com.team.child_be.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.child_be.dtos.enums.ProviderType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_name", columnList = "name"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_phone_number", columnList = "phone_number"),
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long parentId;

    @NotBlank(message = "Tên hiển thị không được để trống")
    // @Min(value = 3, message = "Tên hiển thị phải có ít nhất 3 ký tự")
    String name;

    @Email(message = "Tên đăng nhập có định dạng email không hợp lệ")
    @NotBlank(message = "Tên đăng nhập không được để trống")
    String username;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @JsonIgnore
    String password;
    boolean enabled;

    String verificationCode;
    String accessCode;

    String resetPasswordToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnore
    Set<Role> roles;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Số điện thoại không hợp lệ")
    @Size(min = 10, max = 15, message = "Số điện thoại phải có từ 10 đến 15 ký tự")
    String phoneNumber;

    String avatarCode;
    ProviderType providerType;

    Double totalPoints;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime deletedAt;
}