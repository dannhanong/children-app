package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.AppBlockedRequest;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.AppBlockedService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/blocked")
public class AppBlockedController {
    @Autowired
    private AppBlockedService appBlockedService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/app/add")
    public ResponseEntity<?> addAppBlocked(HttpServletRequest request, @RequestBody AppBlockedRequest appBlockedRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.addAppBlocked(username, appBlockedRequest));
    }

    @PostMapping("/web/add")
    public ResponseEntity<?> addWebBlocked(HttpServletRequest request, @RequestBody AppBlockedRequest appBlockedRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.addWebBlocked(username, appBlockedRequest));
    }

    @DeleteMapping("/app/delete/{id}")
    public ResponseEntity<?> deleteAppBlocked(HttpServletRequest request, @PathVariable Long id) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.deleteAppBlocked(username, id));
    }

    @GetMapping("/app/{childId}")
    public ResponseEntity<?> getMyChildAppBlocked(HttpServletRequest request, @PathVariable Long childId) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.getMyChildAppBlocked(username, childId));
    }

    @GetMapping("/all/app")
    public ResponseEntity<?> getAllAppBlocked(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.getAllAppBlocked(username));
    }

    @GetMapping("/all/web")
    public ResponseEntity<?> getAllWebBlocked(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.getAllWebBlocked(username));
    }

    @GetMapping("/all/app/child")
    public ResponseEntity<?> getAllAppBlockedByChild(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.getAllAppBlockedByChild(username));
    }

    @GetMapping("/all/web/child")
    public ResponseEntity<?> getAllWebBlockedByChild(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.getAllWebBlockedByChild(username));
    }

    @GetMapping("/web/{childId}")
    public ResponseEntity<?> getMyChildWebBlocked(HttpServletRequest request, @PathVariable Long childId) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(appBlockedService.getMyChildWebBlocked(username, childId));
    }
}
