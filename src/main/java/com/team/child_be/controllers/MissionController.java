package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.MissionRequest;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.MissionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/missions")
public class MissionController {
    @Autowired
    private MissionService missionService;
    @Autowired
    private JwtService jwtService;

    @GetMapping("/my-missions")
    public ResponseEntity<?> getMyMissions(HttpServletRequest request,
                                            @RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id") String sortBy,
                                            @RequestParam(defaultValue = "asc") String order) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sortBy));
            return ResponseEntity.ok(missionService.getMyMissions(username, keyword, pageable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách nhiệm vụ: " + e.getMessage());
        }
    }

    @PostMapping("/parent/create")
    public ResponseEntity<?> createMission(HttpServletRequest request,
                                            @RequestBody MissionRequest missionRequest) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(missionService.createMission(missionRequest, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo nhiệm vụ: " + e.getMessage());
        }
    }
}
