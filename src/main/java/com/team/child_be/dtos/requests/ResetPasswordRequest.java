package com.team.child_be.dtos.requests;

public record ResetPasswordRequest(
    String newPassword,
    String confirmPassword
) {}
