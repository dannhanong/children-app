package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record FolderRequest(
    @NotBlank(message = "Tên thư mục không được để trống")
    String name
) {}
