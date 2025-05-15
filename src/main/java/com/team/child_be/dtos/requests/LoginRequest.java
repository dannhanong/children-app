package com.team.child_be.dtos.requests;

import lombok.*;

@Builder
public record LoginRequest(
    String email,
    String password,
    String accessCode,
    boolean rememberMe
) {}
