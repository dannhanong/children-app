package com.team.child_be.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChildResponse {
    private Long id;
    private String name;
    private String username;
    private String phoneNumber;
    private String avatarCode;
    private String accessCode;
    private LocalDateTime createdAt;
    private boolean enabled;
}
