package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.ChangePasswordRequest;
import com.team.child_be.dtos.requests.UpdateProfileRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.dtos.responses.UserProfile;
import com.team.child_be.models.User;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            UserProfile userProfile = userService.getProfile(username);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi lấy thông tin người dùng: " + e.getMessage()));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @ModelAttribute UpdateProfileRequest updateProfileRequest) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            userService.updateProfile(updateProfileRequest, username);
            return ResponseEntity.ok(new ResponseMessage(200, "Cập nhật thông tin thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi cập nhật thông tin người dùng: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> searchByKeyword(@RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id") String sortBy,
                                            @RequestParam(defaultValue = "desc") String order) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sortBy));
            Page<User> users = userService.searchByKeyword(keyword, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi tìm kiếm người dùng: " + e.getMessage()));
        }
    }

    @GetMapping("/my-parent")
    public ResponseEntity<?> getMyParent(HttpServletRequest request) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            User parent = userService.getMyParent(username);
            return ResponseEntity.ok(parent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi lấy thông tin phụ huynh: " + e.getMessage()));
        }
    }

    @GetMapping("/my-children")
    public ResponseEntity<?> getMyChildren(HttpServletRequest request) {
        try {
            String username = jwtService.getUsernameFromRequest(request);
            return ResponseEntity.ok(userService.getMyChildren(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi lấy danh sách trẻ em: " + e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseMessage> changePassword(HttpServletRequest request,
                                                          @RequestBody ChangePasswordRequest changePasswordRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(userService.changePassword(username, changePasswordRequest));
    }
}
