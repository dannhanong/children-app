package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.models.ChildBattery;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.ChildBatteryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/battery")
public class ChildBatteryController {
    @Autowired
    private ChildBatteryService childBatteryService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/create")
    public ResponseEntity<?> createChildBattery(HttpServletRequest request, 
                                                @Valid @RequestBody ChildBattery childBattery) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(childBatteryService.createChildBattery(username, childBattery));
    }
}
