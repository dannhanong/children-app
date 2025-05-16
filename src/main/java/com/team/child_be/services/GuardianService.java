package com.team.child_be.services;

import com.team.child_be.dtos.requests.GuardianRequest;
import com.team.child_be.dtos.responses.GuardianResponse;

import java.util.List;

public interface GuardianService {
    GuardianResponse addGuardian(GuardianRequest request, String username);
    GuardianResponse updateGuardian(Long id, GuardianRequest request, String username);
    void deleteGuardian(Long id, String username);
    List<GuardianResponse> getGuardiansByChild(String username);
    List<GuardianResponse> getGuardiansByParent(String username);
    GuardianResponse getGuardianById(Long id, String username);
}
