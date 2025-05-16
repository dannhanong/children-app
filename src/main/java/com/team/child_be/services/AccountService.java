package com.team.child_be.services;

import com.team.child_be.dtos.requests.ForgotPasswordRequest;
import com.team.child_be.dtos.requests.LoginRequest;
import com.team.child_be.dtos.requests.SignupRequest;
import com.team.child_be.dtos.responses.LoginResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.User;

public interface AccountService {
    User signup(SignupRequest signupRequest);
    ResponseMessage verify(String token);
    LoginResponse login(LoginRequest loginRequest);
    ResponseMessage checkAccessCode(String accessCode, String username);
    ResponseMessage forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    LoginResponse loginWithAccessCode(String accessCode);
}
