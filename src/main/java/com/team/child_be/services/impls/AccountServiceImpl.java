package com.team.child_be.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.dtos.requests.ForgotPasswordRequest;
import com.team.child_be.dtos.requests.LoginRequest;
import com.team.child_be.dtos.requests.SignupRequest;
import com.team.child_be.dtos.responses.LoginResponse;
import com.team.child_be.dtos.responses.NotificationEvent;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.Role;
import com.team.child_be.models.User;
import com.team.child_be.repositories.DeviceTokenRepository;
import com.team.child_be.repositories.RoleRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.AccountService;
import com.team.child_be.services.ConversationService;
import com.team.child_be.services.EmailService;
import com.team.child_be.services.UserService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Override
    public User signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.email())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByPhoneNumber(signupRequest.phoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (!signupRequest.password().equals(signupRequest.confirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }

        Set<Role> roles = new HashSet<>();
        String role = signupRequest.role();
        if (role == null){
            Role userRole = roleRepository.findByName(RoleName.PARENT);
            roles.add(userRole);
        }else {
            switch (role){
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ADMIN);
                    Role adminParentRole = roleRepository.findByName(RoleName.PARENT);
                    Role adminChildRole = roleRepository.findByName(RoleName.CHILD);
                    roles.add(adminRole);
                    roles.add(adminParentRole);
                    roles.add(adminChildRole);
                    break;
                case "parent":
                    Role parentRole = roleRepository.findByName(RoleName.PARENT);
                    Role parentChild = roleRepository.findByName(RoleName.CHILD);
                    roles.add(parentRole);
                    roles.add(parentChild);
                    break;
                case "child":
                    Role childRole = roleRepository.findByName(RoleName.CHILD);
                    roles.add(childRole);
                    break;
            }
        }

        User user = User.builder()
            .name(signupRequest.name())
            .username(signupRequest.email())
            .phoneNumber(signupRequest.phoneNumber())
            .password(passwordEncoder.encode(signupRequest.password()))
            .roles(roles)
            .enabled(true)
            .verificationCode(UUID.randomUUID().toString())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        User savedUser = userRepository.save(user);

        conversationService.createDefaultConversation(savedUser.getId());

        return userRepository.save(savedUser);
    }

    @Override
    public ResponseMessage verify(String token) {
        User user = userRepository.findByVerificationCode(token);
        if (user == null) {
            throw new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }
        user.setEnabled(true);
        user.setVerificationCode(null);

        userRepository.save(user);

        return ResponseMessage.builder()
            .status(200)
            .message("Xác thực tài khoản thành công")
            .build();
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        LoginResponse tokens = new LoginResponse();
        try {
            User user = userRepository.findByUsername(loginRequest.email());
            if (user == null) {
                throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
            }
            boolean isOnlyChild = user.getRoles().size() == 1 &&
                    user.getRoles().stream()
                            .anyMatch(role -> role.getName().equals(RoleName.CHILD));
            if (isOnlyChild) {
                if (loginRequest.accessCode() == null || loginRequest.accessCode().isEmpty()) {
                    throw new RuntimeException("Trẻ em cần cung cấp mã truy cập để đăng nhập");
                }

                if (!user.getAccessCode().equals(loginRequest.accessCode())) {
                    throw new RuntimeException("Mã truy cập không chính xác");
                }
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

            if (!userService.isEnableUser(loginRequest.email())) {
                throw new RuntimeException("Tài khoản chưa được xác minh hoặc đã bị khóa");
            }

            if (authentication.isAuthenticated()) {
                final String accessToken = jwtService.generateToken(loginRequest.email(),
                        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                final String refreshToken = jwtService.generateRefreshToken(loginRequest.email(),
                        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

                tokens = LoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userProfile(userService.getProfile(loginRequest.email()))
                        .build();
            }
        } catch (AuthenticationException e) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        return tokens;
    }

    @Override
    public ResponseMessage checkAccessCode(String accessCode, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Tên đăng nhập không tồn tại");
        }
        if (user.getAccessCode() == null || user.getAccessCode().isEmpty()) {
            throw new RuntimeException("Người dùng này không có mã truy cập");
        }
        if (!user.getAccessCode().equals(accessCode)) {
            throw new RuntimeException("Mã truy cập không chính xác");
        }

        return ResponseMessage.builder()
                .status(200)
                .message("Mã truy cập hợp lệ")
                .build();
    }

    @Override
    public ResponseMessage forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByUsername(forgotPasswordRequest.email());

        if (user == null) {
            throw new RuntimeException("Tên đăng nhập không tồn tại");
        }

        String newPassword = UUID.randomUUID().toString().substring(0, 6);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getUsername())
                .nameOfRecipient(user.getName())
                .subject("Quên mật khẩu")
                .body(newPassword)
                .build();

        emailService.sendForgotPasswordEmail(notificationEvent);

        return ResponseMessage.builder()
                .status(200)
                .message("Mật khẩu mới đã được gửi đến email của bạn")
                .build();
    }

    @Override
    public LoginResponse loginWithAccessCode(String accessCode) {
        String username = userRepository.findByAccessCodeAndDeletedAtIsNull(accessCode)
                .orElseThrow(() -> new RuntimeException("Mã truy cập không hợp lệ")).getUsername();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Mã truy cập không hợp lệ");
        }
        if (!user.isEnabled()) {
            throw new RuntimeException("Tài khoản chưa được xác minh hoặc đã bị khóa");
        }

        final String accessToken = jwtService.generateToken(user.getUsername(),
            user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toList()));
        final String refreshToken = jwtService.generateRefreshToken(username,
            user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toList()));

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userProfile(userService.getProfile(username))
            .build();
    }

    @Override
    @Transactional
    public ResponseMessage logout(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Tên đăng nhập không tồn tại");
        }
        // Xóa token thiết bị
        deviceTokenRepository.deleteByUser_Username(username);

        return ResponseMessage.builder()
                .status(200)
                .message("Đăng xuất thành công")
                .build();
    }
}
