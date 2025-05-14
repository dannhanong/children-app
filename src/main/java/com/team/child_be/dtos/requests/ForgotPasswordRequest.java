package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
    @Email(message = "Email không hợp lệ")
    String email
) {}
