package com.team.child_be.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.team.child_be.dtos.requests.MissionRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.Mission;

public interface MissionService {
    ResponseMessage createMission(MissionRequest missionRequest, String username);
    ResponseMessage updateMission(Long missionId, MissionRequest missionRequest, String username);
    ResponseMessage deleteMission(Long missionId, String username);
    Mission getMissionById(Long missionId, String username);
    ResponseMessage completeMission(Long missionId, String username);
    ResponseMessage confirmMissionCompleted(Long missionId, String username, boolean confirm);
    Page<Mission> getMyMissions(String username, String keyword, Pageable pageable);
}
