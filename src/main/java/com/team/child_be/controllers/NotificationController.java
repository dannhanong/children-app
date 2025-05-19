package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.FCMService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private FCMService fcmService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/sos")
    public ResponseEntity<?> sendSos(HttpServletRequest request) throws Exception {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(fcmService.sendSosNotification(username));
    }
}
