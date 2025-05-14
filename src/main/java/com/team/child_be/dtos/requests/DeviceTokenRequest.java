package com.team.child_be.dtos.requests;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
    @NotBlank(message = "Token không được để trống")
    String token,
    
    @NotBlank(message = "Tên thiết bị không được để trống")
    String deviceName,
    
    @NotBlank(message = "Model thiết bị không được để trống")
    String deviceModel
) {}
