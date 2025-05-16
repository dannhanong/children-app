package com.team.child_be.controllers;

import com.team.child_be.dtos.requests.GuardianRequest;
import com.team.child_be.dtos.responses.GuardianResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.services.GuardianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/guardians")
public class GuardianController {

    @Autowired
    private GuardianService guardianService;

    @PostMapping
    @PreAuthorize("hasAuthority('PARENT')")
    public ResponseEntity<GuardianResponse> addGuardian(
            @Valid @RequestBody GuardianRequest request,
            Authentication authentication) {
        GuardianResponse response = guardianService.addGuardian(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PARENT')")
    public ResponseEntity<GuardianResponse> updateGuardian(
            @PathVariable Long id,
            @Valid @RequestBody GuardianRequest request,
            Authentication authentication) {
        GuardianResponse response = guardianService.updateGuardian(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PARENT')")
    public ResponseEntity<ResponseMessage> deleteGuardian(
            @PathVariable Long id,
            Authentication authentication) {
        guardianService.deleteGuardian(id, authentication.getName());
        return ResponseEntity.ok(
                ResponseMessage.builder()
                        .status(200)
                        .message("Xóa người giám hộ thành công")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<List<GuardianResponse>> getGuardians(
            Authentication authentication) {
        List<GuardianResponse> guardians;
        
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("PARENT"))) {
            guardians = guardianService.getGuardiansByParent(authentication.getName());
        } else {
            guardians = guardianService.getGuardiansByChild(authentication.getName());
        }
        
        return ResponseEntity.ok(guardians);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuardianResponse> getGuardianById(
            @PathVariable Long id,
            Authentication authentication) {
        GuardianResponse guardian = guardianService.getGuardianById(id, authentication.getName());
        return ResponseEntity.ok(guardian);
    }
}
