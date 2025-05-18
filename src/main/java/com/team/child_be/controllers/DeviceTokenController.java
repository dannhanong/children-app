package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.team.child_be.dtos.requests.DeviceTokenRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.FCMService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/device-tokens")
public class DeviceTokenController {
    @Autowired
    private FCMService fcmService;
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<ResponseMessage> registerToken(
            // @AuthenticationPrincipal User currentUser,
            HttpServletRequest request,
            @Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        fcmService.registerDeviceToken(username, deviceTokenRequest);
        
        return ResponseEntity.ok(new ResponseMessage(200, "Đăng ký token thành công"));
    }

    // @PostMapping("/register")
    // public ResponseEntity<ResponseMessage> registerToken(
    //         @Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
    //     fcmService.registerDeviceToken(deviceTokenRequest);
        
    //     return ResponseEntity.ok(new ResponseMessage(200, "Đăng ký token thành công"));
    // }
    
    @DeleteMapping("/unregister")
    public ResponseEntity<ResponseMessage> unregisterToken(
            HttpServletRequest request,
            @Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        fcmService.deactivateDeviceToken(username, deviceTokenRequest.token());
        return ResponseEntity.ok(new ResponseMessage(200, "Hủy đăng ký token thành công"));
    }
}
