package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.MissionRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
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

    @PutMapping("/parent/update/{missionId}")
    public ResponseEntity<?> updateMission(HttpServletRequest request,
                                            @RequestBody MissionRequest missionRequest,
                                            @PathVariable Long missionId) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(missionService.updateMission(missionId, missionRequest, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật nhiệm vụ: " + e.getMessage());
        }
    }

    @PutMapping("/child/complete/{missionId}")
    public ResponseEntity<?> completeMission(HttpServletRequest request,
                                              @PathVariable Long missionId) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(missionService.completeMission(missionId, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hoàn thành nhiệm vụ: " + e.getMessage());
        }
    }

    @PutMapping("/parent/confirm/{missionId}")
    public ResponseEntity<?> confirmMission(HttpServletRequest request,
                                             @PathVariable Long missionId,
                                             @RequestParam boolean confirm) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(missionService.confirmMissionCompleted(missionId, username, confirm));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi xác nhận nhiệm vụ: " + e.getMessage()));
        }
    }

    @DeleteMapping("/parent/delete/{missionId}")
    public ResponseEntity<?> deleteMission(HttpServletRequest request,
                                            @PathVariable Long missionId) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(missionService.deleteMission(missionId, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi xóa nhiệm vụ: " + e.getMessage()));
        }
    }

    @GetMapping("/get/{missionId}")
    public ResponseEntity<?> getMissionById(HttpServletRequest request,
                                             @PathVariable Long missionId) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(missionService.getMissionById(missionId, username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi lấy nhiệm vụ: " + e.getMessage()));
        }
    }
}
