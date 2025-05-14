package com.team.child_be.services.impls;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.child_be.dtos.requests.MissionRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.Mission;
import com.team.child_be.models.User;
import com.team.child_be.repositories.MissionRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.MissionService;

@Service
public class MissionServiceImpl implements MissionService{
    @Autowired
    private MissionRepository missionRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseMessage createMission(MissionRequest missionRequest, String username) {
        User parent = userRepository.findByUsername(username);
        User child = userRepository.findById(missionRequest.childId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trẻ em"));
        missionRepository.save(Mission.builder()
                .parent(parent)
                .child(child)
                .title(missionRequest.title())
                .description(missionRequest.description())
                .deadline(missionRequest.deadline())
                .points(missionRequest.points())
                .build());
        return ResponseMessage.builder()
                .status(200)
                .message("Tạo nhiệm vụ thành công")
                .build();
    }

    @Override
    public ResponseMessage updateMission(Long missionId, MissionRequest missionRequest, String username) {
        User parent = userRepository.findByUsername(username);
        User child = userRepository.findById(missionRequest.childId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trẻ em"));

        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhiệm vụ"));

        if (parent.getId() != mission.getParent().getId()) {
            throw new RuntimeException("Bạn không có quyền sửa nhiệm vụ này");
        }

        mission.setChild(child);
        mission.setTitle(missionRequest.title());
        mission.setDescription(missionRequest.description());
        mission.setDeadline(missionRequest.deadline());
        mission.setPoints(missionRequest.points());
        missionRepository.save(mission);

        return ResponseMessage.builder()
                .status(200)
                .message("Cập nhật nhiệm vụ thành công")
                .build();
    }

    @Override
    public ResponseMessage deleteMission(Long missionId, String username) {
        User parent = userRepository.findByUsername(username);
        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhiệm vụ"));
        
        if (parent.getId() != mission.getParent().getId()) {
            throw new RuntimeException("Bạn không có quyền xóa nhiệm vụ này");
        }

        mission.setDeletedAt(LocalDateTime.now());
        missionRepository.save(mission);

        return ResponseMessage.builder()
                .status(200)
                .message("Xóa nhiệm vụ thành công")
                .build();
    }

    @Override
    public Mission getMissionById(Long missionId, String username) {
        User user = userRepository.findByUsername(username);

        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhiệm vụ"));

        if (user.getId() != mission.getParent().getId() || user.getId() != mission.getChild().getId()) {
            throw new RuntimeException("Bạn không có quyền xem nhiệm vụ này");
        }

        return mission;
    }
    
}
