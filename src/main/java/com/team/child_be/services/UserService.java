package com.team.child_be.services;

import com.team.child_be.dtos.requests.ChangePasswordRequest;
import com.team.child_be.dtos.requests.ResetPasswordRequest;
import com.team.child_be.dtos.requests.UpdateProfileRequest;
import com.team.child_be.dtos.responses.ChildInfoResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.dtos.responses.UserAllInfo;
import com.team.child_be.dtos.responses.UserProfile;
import com.team.child_be.models.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User createUser(User user);
    User createUserByAdmin(User user);
    boolean existsByUsername(String username);
    Page<User> searchByKeyword(String keyword, Pageable pageable);
    ResponseMessage verify(String verificationCode);
    boolean isEnableUser(String username);
    ResponseMessage changePassword(String username, ChangePasswordRequest changePasswordRequest);
    User getUserByResetPasswordToken(String resetPasswordToken);
    ResponseMessage resetPassword(String resetPasswordToken, ResetPasswordRequest resetPasswordRequest);
    User oauth2Authenticate(String code);
    ResponseMessage deleteUser(String username);
    Page<User> getAllUserAndByKeyword(Pageable pageable, String keyword);
    Page<UserAllInfo> getAllUserInfoAndByKeyword(Pageable pageable, String keyword);
    ResponseMessage updateProfile(UpdateProfileRequest updateProfileRequest, String username);
    UserProfile getProfile(String username);
    User getMyParent(String username);
    List<ChildInfoResponse> getMyChildren(String username);
}
