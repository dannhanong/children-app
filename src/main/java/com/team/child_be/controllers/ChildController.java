package com.team.child_be.controllers;

import com.team.child_be.dtos.requests.ChildRequest;
import com.team.child_be.dtos.responses.ChildResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.services.ChildService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children")
@Slf4j
public class ChildController {

    @Autowired
    private ChildService childService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<List<ChildResponse>> getAllChildren(Authentication authentication) {
        log.info("API GET /api/children - Lấy danh sách trẻ");
        String username = authentication.getName();
        List<ChildResponse> children = childService.getAllChildren(username);
        return ResponseEntity.ok(children);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<ChildResponse> getChildById(@PathVariable Long id, Authentication authentication) {
        log.info("API GET /api/children/{} - Lấy thông tin chi tiết trẻ", id);
        String username = authentication.getName();
        ChildResponse child = childService.getChildById(id, username);
        return ResponseEntity.ok(child);
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<List<ChildResponse>> searchChildren(@RequestParam String keyword, Authentication authentication) {
        log.info("API GET /api/children/search - Tìm kiếm trẻ với từ khóa: '{}'", keyword);
        String username = authentication.getName();
        List<ChildResponse> children = childService.searchChildren(keyword, username);
        return ResponseEntity.ok(children);
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<ChildResponse> createChild(@Valid @ModelAttribute ChildRequest childRequest, 
                                                    Authentication authentication) {
        log.info("API POST /api/children - Tạo tài khoản trẻ mới");
        String username = authentication.getName();
        ChildResponse child = childService.createChild(childRequest, username);
        return new ResponseEntity<>(child, HttpStatus.CREATED);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<ChildResponse> updateChild(@PathVariable Long id, 
                                                   @Valid @ModelAttribute ChildRequest childRequest, 
                                                   Authentication authentication) {
        log.info("API PUT /api/children/{} - Cập nhật thông tin trẻ", id);
        String username = authentication.getName();
        ChildResponse child = childService.updateChild(id, childRequest, username);
        return ResponseEntity.ok(child);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<ResponseMessage> deleteChild(@PathVariable Long id, Authentication authentication) {
        log.info("API DELETE /api/children/{} - Xóa tài khoản trẻ", id);
        String username = authentication.getName();
        ResponseMessage response = childService.deleteChild(id, username);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/access-code")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PARENT')")
    public ResponseEntity<ChildResponse> regenerateAccessCode(@PathVariable Long id, Authentication authentication) {
        log.info("API PUT /api/children/{}/access-code - Tạo mã truy cập mới", id);
        String username = authentication.getName();
        ChildResponse child = childService.regenerateAccessCode(id, username);
        return ResponseEntity.ok(child);
    }
}
