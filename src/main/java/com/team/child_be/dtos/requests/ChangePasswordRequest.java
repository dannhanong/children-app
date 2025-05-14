package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank @Min(6)
    String oldPassword,
    @NotBlank @Min(6)
    String newPassword,
    @NotBlank @Min(6)
    String confirmNewPassword
) {}
