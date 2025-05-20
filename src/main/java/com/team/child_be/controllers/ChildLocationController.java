package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.ChildLocationRequest;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.ChildLocationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/locations")
public class ChildLocationController {
    @Autowired
    private ChildLocationService childLocationService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/add")
    public ResponseEntity<?> addChildLocation(HttpServletRequest request, 
                                                @RequestBody ChildLocationRequest childLocationRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(childLocationService.addChildLocation(username, childLocationRequest));
    }

    @GetMapping("/family")
    public ResponseEntity<?> getFamilyLocations(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(childLocationService.getFamilyLocations(username));
    }
}
