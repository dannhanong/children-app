package com.team.child_be.services.impls;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.dtos.requests.GuardianRequest;
import com.team.child_be.dtos.responses.GuardianResponse;
import com.team.child_be.models.Guardian;
import com.team.child_be.models.Role;
import com.team.child_be.models.User;
import com.team.child_be.repositories.GuardianRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.GuardianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GuardianServiceImpl implements GuardianService {

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public GuardianResponse addGuardian(GuardianRequest request, String username) {
        User parent = userRepository.findByUsername(username);
        if (parent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        if (!hasParentRole(parent)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ phụ huynh mới có quyền thêm người giám hộ");
        }
        
        List<Long> childrenIds = request.getChildrenIds();
        if (childrenIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần chọn ít nhất một trẻ");
        }
        
        User primaryChild = userRepository.findById(childrenIds.get(0))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Không tìm thấy trẻ chính có ID: " + childrenIds.get(0)));
        
        if (primaryChild.getParentId() == null || !primaryChild.getParentId().equals(parent.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Bạn chỉ có thể thêm người giám hộ cho con của mình");
        }
        
        if (!hasChildRole(primaryChild)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Chỉ có thể thêm người giám hộ cho tài khoản trẻ em");
        }
        
        Guardian guardian = Guardian.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .primaryChild(primaryChild)
                .createdBy(parent)
                .additionalChildren(new HashSet<>())
                .build();
        
        Guardian savedGuardian = guardianRepository.save(guardian);
        
        if (childrenIds.size() > 1) {
            for (int i = 1; i < childrenIds.size(); i++) {
                Long childId = childrenIds.get(i);
                User additionalChild = userRepository.findById(childId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Không tìm thấy trẻ bổ sung có ID: " + childId));
                            
                if (additionalChild.getParentId() == null || !additionalChild.getParentId().equals(parent.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Bạn chỉ có thể thêm người giám hộ cho con của mình");
                }
                
                if (!hasChildRole(additionalChild)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Chỉ có thể thêm người giám hộ cho tài khoản trẻ em");
                }
                
                savedGuardian.getAdditionalChildren().add(additionalChild);
            }
            
            savedGuardian = guardianRepository.save(savedGuardian);
        }
        
        return convertToResponse(savedGuardian);
    }

    @Override
    @Transactional
    public GuardianResponse updateGuardian(Long id, GuardianRequest request, String username) {
        User parent = userRepository.findByUsername(username);
        if (parent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        if (!hasParentRole(parent)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ phụ huynh mới có quyền sửa người giám hộ");
        }
        
        Guardian guardian = guardianRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người giám hộ"));
        
        if (!guardian.getCreatedBy().getId().equals(parent.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Bạn không có quyền sửa thông tin người giám hộ này");
        }
        
        guardian.setName(request.getName());
        guardian.setPhoneNumber(request.getPhoneNumber());
        
        List<Long> childrenIds = request.getChildrenIds();
        if (childrenIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần chọn ít nhất một trẻ");
        }
        
        Long newPrimaryChildId = childrenIds.get(0);
        if (!guardian.getPrimaryChild().getId().equals(newPrimaryChildId)) {
            User newPrimaryChild = userRepository.findById(newPrimaryChildId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Không tìm thấy trẻ chính mới có ID: " + newPrimaryChildId));
            
            if (newPrimaryChild.getParentId() == null || !newPrimaryChild.getParentId().equals(parent.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn chỉ có thể chọn con của mình làm người được giám hộ");
            }
            
            if (!hasChildRole(newPrimaryChild)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Chỉ có thể thêm người giám hộ cho tài khoản trẻ em");
            }
            
            guardian.setPrimaryChild(newPrimaryChild);
        }
        
        guardian.getAdditionalChildren().clear();
        
        if (childrenIds.size() > 1) {
            for (int i = 1; i < childrenIds.size(); i++) {
                Long childId = childrenIds.get(i);
                User additionalChild = userRepository.findById(childId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Không tìm thấy trẻ bổ sung có ID: " + childId));
                            
                if (additionalChild.getParentId() == null || !additionalChild.getParentId().equals(parent.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Bạn chỉ có thể thêm người giám hộ cho con của mình");
                }
                
                if (!hasChildRole(additionalChild)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Chỉ có thể thêm người giám hộ cho tài khoản trẻ em");
                }
                
                guardian.getAdditionalChildren().add(additionalChild);
            }
        }
        
        Guardian updatedGuardian = guardianRepository.save(guardian);
        
        return convertToResponse(updatedGuardian);
    }

    @Override
    @Transactional
    public void deleteGuardian(Long id, String username) {
        User parent = userRepository.findByUsername(username);
        if (parent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        if (!hasParentRole(parent)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ phụ huynh mới có quyền xóa người giám hộ");
        }
        
        Guardian guardian = guardianRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người giám hộ"));
        
        if (!guardian.getCreatedBy().getId().equals(parent.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Bạn không có quyền xóa người giám hộ này");
        }
        
        guardianRepository.delete(guardian);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuardianResponse> getGuardiansByChild(String username) {
        User child = userRepository.findByUsername(username);
        if (child == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        
        List<Guardian> guardians = guardianRepository.findByAnyChildId(child.getId());
        
        return guardians.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuardianResponse> getGuardiansByParent(String username) {
        User parent = userRepository.findByUsername(username);
        if (parent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        
        List<Guardian> guardians = guardianRepository.findByCreatedById(parent.getId());
        
        return guardians.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GuardianResponse getGuardianById(Long id, String username) {
        Guardian guardian = guardianRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người giám hộ"));
        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        
        boolean hasAccess = false;
        
        if (hasParentRole(user)) {
            hasAccess = guardian.getCreatedBy().getId().equals(user.getId());
        } else {
            hasAccess = guardian.getPrimaryChild().getId().equals(user.getId()) ||
                        guardian.getAdditionalChildren().stream()
                                .anyMatch(child -> child.getId().equals(user.getId()));
        }
        
        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem thông tin người giám hộ này");
        }
        
        return convertToResponse(guardian);
    }
    
    private GuardianResponse convertToResponse(Guardian guardian) {
        User createdBy = guardian.getCreatedBy();
        
        List<GuardianResponse.ChildInfo> childInfoList = new ArrayList<>();
        
        childInfoList.add(GuardianResponse.ChildInfo.builder()
                .id(guardian.getPrimaryChild().getId())
                .name(guardian.getPrimaryChild().getName())
                .isPrimary(true)
                .build());
        
        guardian.getAdditionalChildren().forEach(child -> {
            childInfoList.add(GuardianResponse.ChildInfo.builder()
                    .id(child.getId())
                    .name(child.getName())
                    .isPrimary(false)
                    .build());
        });
        
        return GuardianResponse.builder()
                .id(guardian.getId())
                .name(guardian.getName())
                .phoneNumber(guardian.getPhoneNumber())
                .children(childInfoList)
                .createdById(createdBy.getId())
                .createdByName(createdBy.getName())
                .createdAt(guardian.getCreatedAt())
                .updatedAt(guardian.getUpdatedAt())
                .build();
    }
    
    private boolean hasParentRole(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName == RoleName.PARENT);
    }
    
    private boolean hasChildRole(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName -> roleName == RoleName.CHILD);
    }
}
