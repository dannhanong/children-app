package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.ForgotPasswordRequest;
import com.team.child_be.dtos.requests.LoginRequest;
import com.team.child_be.dtos.requests.SignupRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.AccountService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signup(@Valid @RequestBody SignupRequest signupRequest) {        
        try {
            accountService.signup(signupRequest);
            return ResponseEntity.ok(new ResponseMessage(200, "Đăng ký thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi đăng ký người dùng: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(accountService.login(loginRequest));
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);

        accountService.logout(username);
        
        String token = request.getHeader("Authorization").substring(7);
        jwtService.deleteToken(token);
        return new ResponseEntity<>(new ResponseMessage(200, "Đăng xuất thành công"), HttpStatus.OK);
    }

    @PostMapping("/child/login/{code}")
    public ResponseEntity<?> login(@PathVariable String code) {
        try {
            return ResponseEntity.ok(accountService.loginWithAccessCode(code));
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        try {
            ResponseMessage responseMessage = accountService.verify(token);
            if (responseMessage.getStatus() == 200) {
                return ResponseEntity.ok(responseMessage);
            } else {
                return ResponseEntity.badRequest().body(responseMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi xác thực: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            accountService.forgotPassword(forgotPasswordRequest);
            return ResponseEntity.ok(new ResponseMessage(200, "Đã gửi email đặt lại mật khẩu"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(400, "Lỗi gửi email: " + e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        jwtService.validateToken(token);
        return "true";
    }
}
