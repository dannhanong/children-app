package com.team.child_be.services.impls;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.dtos.requests.ChildRequest;
import com.team.child_be.dtos.responses.ChildResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.FileUpload;
import com.team.child_be.models.Role;
import com.team.child_be.models.User;
import com.team.child_be.repositories.RoleRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.ChildService;
import com.team.child_be.services.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ChildServiceImpl implements ChildService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ChildResponse createChild(ChildRequest childRequest, String username) {
        log.info("Tạo tài khoản trẻ mới cho phụ huynh: {}", username);

        User parent = userRepository.findByUsername(username);
        if (parent == null) {
            log.error("Không tìm thấy thông tin phụ huynh với username: {}", username);
            throw new RuntimeException("Không tìm thấy thông tin phụ huynh");
        }
        if (userRepository.existsByPhoneNumber(childRequest.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        String accessCode = generateAccessCode();
        log.debug("Đã tạo mã truy cập: {}", accessCode);

        String parentEmail = parent.getUsername();
        String childUsername = parentEmail.split("@")[0] + "." + accessCode + "@" + parentEmail.split("@")[1];
        log.debug("Username được tạo cho trẻ: {}", childUsername);

        Role childRole = roleRepository.findByName(RoleName.CHILD);
        if (childRole == null) {
            log.error("Không tìm thấy vai trò CHILD trong hệ thống");
            throw new RuntimeException("Không tìm thấy vai trò CHILD trong hệ thống");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(childRole);

        User child = User.builder()
                .name(childRequest.getName())
                .username(childUsername)
                .password(passwordEncoder.encode(childRequest.getPassword())) // Sử dụng password từ request
                .phoneNumber(childRequest.getPhoneNumber())
                .enabled(true)
                .roles(roles)
                .parentId(parent.getId())
                .accessCode(accessCode) // Vẫn lưu accessCode riêng
                .createdAt(LocalDateTime.now())
                .build();

        MultipartFile avatar = childRequest.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            try {
                log.debug("Xử lý file avatar");
                FileUpload fileUpload = fileUploadService.uploadFile(avatar);
                child.setAvatarCode(fileUpload.getFileCode());
                log.debug("Đã lưu avatarCode: {}", fileUpload.getFileCode());
            } catch (Exception e) {
                log.error("Lỗi khi tải ảnh lên: {}", e.getMessage(), e);
                throw new RuntimeException("Lỗi khi tải ảnh lên");
            }
        }

        User savedChild = userRepository.save(child);
        log.info("Đã tạo tài khoản trẻ thành công với ID: {}", savedChild.getId());

        return mapToChildResponse(savedChild);
    }

    @Override
    public ChildResponse updateChild(Long id, ChildRequest childRequest, String username) {
        log.info("Cập nhật thông tin trẻ ID: {} bởi phụ huynh: {}", id, username);
        
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        User child;
        if (isAdmin) {
            child = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ với ID: {}", id);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
            
            if (child.getDeletedAt() != null ||
                !child.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CHILD)) {
                log.error("Người dùng ID: {} không phải là trẻ em hoặc đã bị xóa", id);
                throw new RuntimeException("Không tìm thấy thông tin trẻ");
            }
        } else {
            User parent = userRepository.findByUsername(username);
            child = userRepository.findChildByIdAndParentId(id, parent.getId())
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ ID: {} thuộc phụ huynh: {}", id, username);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
        }

        child.setName(childRequest.getName());
        child.setPhoneNumber(childRequest.getPhoneNumber());
        child.setUpdatedAt(LocalDateTime.now());
        log.debug("Cập nhật thông tin cơ bản cho trẻ ID: {}", id);

        MultipartFile avatar = childRequest.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            try {
                log.debug("Xử lý file avatar mới cho trẻ ID: {}", id);
                String oldAvatarCode = child.getAvatarCode();
                FileUpload fileUpload = fileUploadService.uploadFile(avatar);
                child.setAvatarCode(fileUpload.getFileCode());
                log.debug("Đã lưu avatarCode mới: {}", fileUpload.getFileCode());

                if (oldAvatarCode != null && !oldAvatarCode.isEmpty()) {
                    log.debug("Xóa avatar cũ: {}", oldAvatarCode);
                    fileUploadService.deleteFileByFileCode(oldAvatarCode);
                }
            } catch (Exception e) {
                log.error("Lỗi khi tải ảnh lên: {}", e.getMessage(), e);
                throw new RuntimeException("Lỗi khi tải ảnh lên");
            }
        }

        User updatedChild = userRepository.save(child);
        log.info("Đã cập nhật thông tin trẻ ID: {} thành công", id);
        
        return mapToChildResponse(updatedChild);
    }

    @Override
    public ResponseMessage deleteChild(Long id, String username) {
        log.info("Xóa tài khoản trẻ ID: {} bởi phụ huynh: {}", id, username);
        
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        User child;
        if (isAdmin) {
            child = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ với ID: {}", id);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
            
            if (child.getDeletedAt() != null ||
                !child.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CHILD)) {
                log.error("Người dùng ID: {} không phải là trẻ em hoặc đã bị xóa", id);
                throw new RuntimeException("Không tìm thấy thông tin trẻ");
            }
        } else {
            User parent = userRepository.findByUsername(username);
            child = userRepository.findChildByIdAndParentId(id, parent.getId())
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ ID: {} thuộc phụ huynh: {}", id, username);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
        }

        child.setDeletedAt(LocalDateTime.now());
        userRepository.save(child);
        log.info("Đã xóa (soft delete) tài khoản trẻ ID: {} thành công", id);

        return ResponseMessage.builder()
                .status(200)
                .message("Xóa thông tin trẻ thành công")
                .build();
    }

    @Override
    public ChildResponse regenerateAccessCode(Long id, String username) {
        log.info("Tạo mã truy cập mới cho trẻ ID: {} bởi phụ huynh: {}", id, username);
        
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        User child;
        if (isAdmin) {
            child = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ với ID: {}", id);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
            
            if (child.getDeletedAt() != null ||
                !child.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CHILD)) {
                log.error("Người dùng ID: {} không phải là trẻ em hoặc đã bị xóa", id);
                throw new RuntimeException("Không tìm thấy thông tin trẻ");
            }
        } else {
            User parent = userRepository.findByUsername(username);
            child = userRepository.findChildByIdAndParentId(id, parent.getId())
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ ID: {} thuộc phụ huynh: {}", id, username);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
        }

        String newAccessCode = generateAccessCode();
        log.debug("Đã tạo mã truy cập mới: {}", newAccessCode);
        
        child.setAccessCode(newAccessCode);
        child.setPassword(passwordEncoder.encode(newAccessCode)); // Cập nhật mật khẩu
        child.setUpdatedAt(LocalDateTime.now());

        User updatedChild = userRepository.save(child);
        log.info("Đã cập nhật mã truy cập mới cho trẻ ID: {} thành công", id);
        
        return mapToChildResponse(updatedChild);
    }

    @Override
    public List<ChildResponse> getAllChildren(String username) {
        log.info("Lấy danh sách trẻ của phụ huynh: {}", username);
        
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        List<User> children;
        if (isAdmin) {
            log.debug("User có quyền ADMIN, lấy danh sách tất cả trẻ em");
            children = userRepository.findAll().stream()
                    .filter(u -> u.getDeletedAt() == null && 
                            u.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CHILD))
                    .collect(Collectors.toList());
        } else {
            log.debug("User có quyền PARENT, lấy danh sách trẻ em thuộc phụ huynh");
            User parent = userRepository.findByUsername(username);
            children = userRepository.findChildrenByParentId(parent.getId());
        }

        log.debug("Tìm thấy {} trẻ em", children.size());
        return children.stream()
                .map(this::mapToChildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChildResponse getChildById(Long id, String username) {
        log.info("Lấy thông tin chi tiết trẻ ID: {} bởi phụ huynh: {}", id, username);
        
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        User child;
        if (isAdmin) {
            log.debug("User có quyền ADMIN, lấy thông tin trẻ em theo ID");
            child = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ với ID: {}", id);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
            
            if (child.getDeletedAt() != null ||
                !child.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CHILD)) {
                log.error("Người dùng ID: {} không phải là trẻ em hoặc đã bị xóa", id);
                throw new RuntimeException("Không tìm thấy thông tin trẻ");
            }
        } else {
            log.debug("User có quyền PARENT, lấy thông tin trẻ em thuộc phụ huynh theo ID");
            User parent = userRepository.findByUsername(username);
            child = userRepository.findChildByIdAndParentId(id, parent.getId())
                    .orElseThrow(() -> {
                        log.error("Không tìm thấy thông tin trẻ ID: {} thuộc phụ huynh: {}", id, username);
                        return new RuntimeException("Không tìm thấy thông tin trẻ");
                    });
        }

        log.info("Đã lấy thông tin chi tiết trẻ ID: {} thành công", id);
        return mapToChildResponse(child);
    }

    @Override
    public List<ChildResponse> searchChildren(String keyword, String username) {
        log.info("Tìm kiếm trẻ với từ khóa: '{}' bởi phụ huynh: {}", keyword, username);
        
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        List<User> children;
        if (isAdmin) {
            log.debug("User có quyền ADMIN, tìm kiếm tất cả trẻ em theo từ khóa");
            children = userRepository.lSearchByKeyword(keyword).stream()
                    .filter(u -> u.getDeletedAt() == null && 
                            u.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CHILD))
                    .collect(Collectors.toList());
        } else {
            log.debug("User có quyền PARENT, tìm kiếm trẻ em thuộc phụ huynh theo từ khóa");
            User parent = userRepository.findByUsername(username);
            children = userRepository.searchChildrenByNameForParent(parent.getId(), keyword);
        }

        log.debug("Tìm thấy {} trẻ em phù hợp với từ khóa: '{}'", children.size(), keyword);
        return children.stream()
                .map(this::mapToChildResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo mã truy cập ngẫu nhiên 6 số
     * 
     * @return Mã truy cập
     */
    private String generateAccessCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        String accessCode = sb.toString();

        if (userRepository.existsByAccessCode(accessCode)) {
            log.debug("Mã truy cập {} đã tồn tại, tạo mã mới", accessCode);
            return generateAccessCode();
        }

        return accessCode;
    }

    /**
     * Chuyển đổi từ entity User sang DTO ChildResponse
     * 
     * @param child Entity User với role CHILD
     * @return DTO ChildResponse
     */
    private ChildResponse mapToChildResponse(User child) {
        return ChildResponse.builder()
                .id(child.getId())
                .name(child.getName())
                .email(child.getUsername())
                .phoneNumber(child.getPhoneNumber())
                .avatarCode(child.getAvatarCode())
                .accessCode(child.getAccessCode())
                .createdAt(child.getCreatedAt())
                .enabled(child.isEnabled())
                .build();
    }
}
