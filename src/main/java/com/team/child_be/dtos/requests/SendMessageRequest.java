package com.team.child_be.dtos.requests;

import org.springframework.web.multipart.MultipartFile;

public record SendMessageRequest(
    String receiverId,
    String message,
    MultipartFile file
) {}
