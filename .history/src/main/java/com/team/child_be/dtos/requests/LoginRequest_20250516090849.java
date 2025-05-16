package com.team.child_be.dtos.requests;

public record LoginRequest(
    String email,
    String password,
    String accessCode,
    boolean rememberMe
) {}
