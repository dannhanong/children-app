package com.team.child_be.services;

import com.team.child_be.dtos.requests.MissionRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.Mission;

public interface MissionService {
    ResponseMessage createMission(MissionRequest missionRequest, String username);
    ResponseMessage updateMission(Long missionId, MissionRequest missionRequest, String username);
    ResponseMessage deleteMission(Long missionId, String username);
    Mission getMissionById(Long missionId, String username);
}
