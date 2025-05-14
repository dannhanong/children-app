package com.team.child_be.dtos.requests;

import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequest(
    String name,
    String phoneNumber,
    MultipartFile avatar
) {}
