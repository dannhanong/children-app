package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record ChatlogFolderRequest(
    @NotNull(message = "ID tin nhắn không được để trống")
    Long chatlogId,
    @NotNull(message = "ID thư mục không được để trống")
    Long folderId
) {}
