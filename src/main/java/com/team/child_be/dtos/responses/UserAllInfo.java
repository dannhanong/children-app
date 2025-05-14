package com.team.child_be.dtos.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAllInfo {
    Long id;
    String name;
    String username;
    String password;
    boolean enabled;
    String verificationCode;
    String resetPasswordToken;
    String roles;
    String phoneNumber;
    String avatarId;
    LocalDateTime createdAt;
}
