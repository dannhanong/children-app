package com.team.child_be.services.impls;

import com.team.child_be.dtos.enums.ProviderType;
import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.dtos.requests.ChangePasswordRequest;
import com.team.child_be.dtos.requests.ExchangeTokenRequest;
import com.team.child_be.dtos.requests.ForgotPasswordRequest;
import com.team.child_be.dtos.requests.ResetPasswordRequest;
import com.team.child_be.dtos.requests.UpdateProfileRequest;
import com.team.child_be.dtos.responses.NotificationEvent;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.dtos.responses.UserAllInfo;
import com.team.child_be.dtos.responses.UserProfile;
import com.team.child_be.http_clients.GoogleOauth2IdentityClient;
import com.team.child_be.http_clients.GoogleOauth2UserClient;
import com.team.child_be.models.FileUpload;
import com.team.child_be.models.Role;
import com.team.child_be.models.User;
import com.team.child_be.repositories.RoleRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.FileUploadService;
import com.team.child_be.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private GoogleOauth2IdentityClient googleOauth2IdentityClient;
    @Autowired
    private GoogleOauth2UserClient googleOauth2UserClient;
    @Value("${oauth2.identity.client-id}")
    protected String CLIENT_ID;
    @Value("${oauth2.identity.client-secret}")
    protected String CLIENT_SECRET;
    @Value("${oauth2.identity.redirect-uri}")
    protected String REDIRECT_URI;
    protected final String GRANT_TYPE = "authorization_code";

    @Override
    public User createUser(User user) {
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setVerificationCode(generateVerificationCode());
        return userRepository.save(user);
    }

    @Override
    public User createUserByAdmin(User user) {
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setVerificationCode(generateVerificationCode());
        return userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public ResponseMessage verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);
        if (user == null || user.isEnabled()) {
            throw new RuntimeException("Mã xác thực không hợp lệ hoặc tài khoản đã được xác thực trước đó");
        } else {
            user.setEnabled(true);
            return new ResponseMessage(200, "Xác thực tài khoản thành công");
        }
    }

    @Override
    public ResponseMessage changePassword(String username, ChangePasswordRequest changePasswordRequest) {
        User currentUser = userRepository.findByUsername(username);
        ResponseMessage responseMessage = new ResponseMessage();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (currentUser == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        if (passwordEncoder.matches(changePasswordRequest.oldPassword(), currentUser.getPassword())) {
            if (!changePasswordRequest.newPassword().equals(changePasswordRequest.confirmNewPassword())) {
                throw new RuntimeException("Mật khẩu không khớp");
            }

            currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
            userRepository.save(currentUser);
            responseMessage.setMessage("Đổi mật khẩu thành công");
        } else {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        return responseMessage;
    }

    @Override
    public ResponseMessage forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByUsername(forgotPasswordRequest.email());
        user.setResetPasswordToken(generateVerificationCode());
        userRepository.save(user);
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getUsername())
                .nameOfRecipient(user.getName())
                .subject("Quên mật khẩu")
                .body(user.getResetPasswordToken())
                .build();
        // kafkaTemplate.send("hdkt-forgot-password", notificationEvent);
        return new ResponseMessage(200, "Gửi mã xác nhận thành công");
    }

    @Override
    public User getUserByResetPasswordToken(String resetPasswordToken) {
        return userRepository.findByResetPasswordToken(resetPasswordToken);
    }

    @Override
    public ResponseMessage resetPassword(String resetPasswordToken, ResetPasswordRequest resetPasswordRequest) {
        ResponseMessage responseMessage = new ResponseMessage();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User currentUser = userRepository.findByResetPasswordToken(resetPasswordToken);
        if (!resetPasswordRequest.newPassword().equals(resetPasswordRequest.confirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }else{
            currentUser.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
            currentUser.setResetPasswordToken(null);
            userRepository.save(currentUser);
            responseMessage.setMessage("Đổi mật khẩu thành công");
            return responseMessage;
        }
    }

    @Override
    public User oauth2Authenticate(String code) {
        var response = googleOauth2IdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        var userInfo = googleOauth2UserClient.getUserInfo("json", response.getAccessToken());
        // log.info("User info: {}", userInfo);

        User user = userRepository.findByUsername(userInfo.getEmail());
        if (user == null) {
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(RoleName.PARENT);
            roles.add(userRole);

            LocalDateTime now = LocalDateTime.now();

            user = User.builder()
                    .name(userInfo.getGivenName() + " " + userInfo.getFamilyName())
                    .username(userInfo.getEmail())
                    .roles(roles)
                    .enabled(true)
                    .createdAt(now)
                    .password("$2a$10$OxGFXT1QNtOHHXXE4G3OVuJ98Mxk6lzMEnFtiH7hQd/LPBmZqQuP.")
                    .providerType(ProviderType.GOOGLE)
                    .build();
            
            User savedUser = userRepository.save(user);

            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(savedUser.getUsername())
                    .nameOfRecipient(savedUser.getName())
                    .subject("Chào mừng bạn đến với trang idai.vn")
                    .body(savedUser.getVerificationCode())
                    .build();
            // kafkaTemplate.send("hdkt-notification-oauth2", notificationEvent);
        }

        return user;
    }

    @Override
    @Transactional
    public ResponseMessage deleteUser(String username) {
        User user = userRepository.findByUsername(username);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getUsername())
                .nameOfRecipient(user.getName())
                .subject("Thông báo về yêu cầu xóa tài khoản")
                .body("Tài khoản của bạn đã bị xóa")
                .build();
                
        return new ResponseMessage(200, "Xóa tài khoản thành công, vui lòng kiểm tra email để xác nhận");
    }

    @Override
    public Page<User> getAllUserAndByKeyword(Pageable pageable, String keyword) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<UserAllInfo> getAllUserInfoAndByKeyword(Pageable pageable, String keyword) {
        List<User> users = userRepository.lSearchByKeyword(keyword);
        List<UserAllInfo> userAllInfos = new ArrayList<>();
        for (User user : users) {
            UserAllInfo userAllInfo = new UserAllInfo();
            userAllInfo.setId(user.getId());
            userAllInfo.setName(user.getName());
            userAllInfo.setUsername(user.getUsername());
            userAllInfo.setEnabled(user.isEnabled());
            userAllInfo.setPhoneNumber(user.getPhoneNumber());
            userAllInfo.setAvatarId(user.getAvatarCode());
            userAllInfo.setEnabled(user.isEnabled());

            userAllInfos.add(userAllInfo);
            Optional<Role> highestRole = user.getRoles().stream()
                    .min(Comparator.comparingInt(this::getRolePriority));

            highestRole.ifPresent(role -> userAllInfo.setRoles(role.getName().name().toLowerCase()));
        }
        return new PageImpl<>(userAllInfos, pageable, userAllInfos.size());
    }

    @Override
    public ResponseMessage updateProfile(UpdateProfileRequest updateProfileRequest, String username) {
        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy user");
        }

        if (userRepository.existsByPhoneNumberAndUsernameNot(updateProfileRequest.phoneNumber(), username)) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }
        currentUser.setName(updateProfileRequest.name());
        currentUser.setPhoneNumber(updateProfileRequest.phoneNumber());

        MultipartFile avatar = updateProfileRequest.avatar();

        if (avatar != null) {
            try {
                String oldFileCode = currentUser.getAvatarCode();
                FileUpload fileUpload = fileUploadService.uploadFile(avatar);
                currentUser.setAvatarCode(fileUpload.getFileCode());

                if (oldFileCode != null && !oldFileCode.isEmpty()) {
                    fileUploadService.deleteFileByFileCode(oldFileCode);
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi tải ảnh lên");
            }
        }

        userRepository.save(currentUser);

        return ResponseMessage.builder()
                .status(200)
                .message("Cập nhật thông tin thành công")
                .build();
    }

    @Override
    public UserProfile getProfile(String username) {        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy user");
        }

        return UserProfile.builder()
                .name(user.getName())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .avatarCode(user.getAvatarCode())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }
        org.springframework.security.core.userdetails.User us = new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), rolesToAuthorities(user.getRoles()));
        return us;
    }

    private Collection<? extends GrantedAuthority> rolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role ->new SimpleGrantedAuthority(role.getName().name())).collect(Collectors.toList());
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString();
    }

    private int getRolePriority(Role role) {
        switch (role.getName().toString()) {
            case "ADMIN":
                return 1;
            case "PARENT":
                return 2;
            case "CHILD":
                return 3;
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean isEnableUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }
        return user.isEnabled();
    }
}
