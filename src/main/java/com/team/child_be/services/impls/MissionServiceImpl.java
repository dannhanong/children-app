package com.team.child_be.services.impls;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.team.child_be.dtos.requests.MissionRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.Mission;
import com.team.child_be.models.User;
import com.team.child_be.repositories.MissionRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.FCMService;
import com.team.child_be.services.MissionService;

@Service
@Transactional
public class MissionServiceImpl implements MissionService{
    @Autowired
    private MissionRepository missionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FCMService fcmService;

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
                .createdAt(LocalDateTime.now())
                .build());

        try {
            fcmService.sendNotificationToUser(child.getId(), "Nhiệm vụ mới", "Bạn có một nhiệm vụ mới từ phụ huynh", "https://firebasestorage.googleapis.com/v0/b/lvkmusic.appspot.com/o/xbn3urb9oc?alt=media&token=b6519c29-eb8b-4d04-aa62-134967c256ea");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        
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

        if (mission.getCompletedAt() != null) {
            throw new RuntimeException("Nhiệm vụ đã hoàn thành, không thể sửa");
        }

        if (mission.getDeletedAt() != null) {
            throw new RuntimeException("Nhiệm vụ đã bị xóa, không thể sửa");
        }

        if (mission.getCreatedAt().isBefore(LocalDateTime.now().minus(1, ChronoUnit.HOURS))) {
            throw new RuntimeException("Nhiệm vụ đã quá hạn để sửa");
        }
        
        if (mission.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Hạn chót không được nhỏ hơn thời gian hiện tại");
        }

        mission.setChild(child);
        mission.setTitle(missionRequest.title());
        mission.setDescription(missionRequest.description());
        mission.setDeadline(missionRequest.deadline());
        mission.setPoints(missionRequest.points());
        mission.setUpdatedAt(LocalDateTime.now());
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

    @Override
    public ResponseMessage completeMission(Long missionId, String username) {
        User child = userRepository.findByUsername(username);
        Mission mission = missionRepository.findByIdAndChild_UsernameAndDeletedAtNull(missionId, username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhiệm vụ"));
        if (mission.getCompletedAt() != null) {
            throw new RuntimeException("Nhiệm vụ đã hoàn thành");
        }

        mission.setCompletedAt(LocalDateTime.now());
        mission.setConfirm(false);
        missionRepository.save(mission);

        try {
            fcmService.sendNotificationToUser(mission.getParent().getId(), 
                "Nhiệm vụ đã hoàn thành", 
                    child.getName() + " đã hoàn thành nhiệm vụ, hãy kiểm tra và xác nhận", 
                    "https://firebasestorage.googleapis.com/v0/b/lvkmusic.appspot.com/o/xbn3urb9oc?alt=media&token=b6519c29-eb8b-4d04-aa62-134967c256ea");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

        return ResponseMessage.builder()
                .status(200)
                .message("Hoàn thành nhiệm vụ thành công")
                .build();
    }

    @Override
    public ResponseMessage confirmMissionCompleted(Long missionId, String username, boolean confirm) {
        User parent = userRepository.findByUsername(username);
        Mission mission = missionRepository.findByIdAndParent_UsernameAndDeletedAtNull(missionId, username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhiệm vụ"));
        
        if (mission.getCompletedAt() == null) {
            throw new RuntimeException("Nhiệm vụ chưa hoàn thành");
        }

        if (confirm) {
            mission.setConfirm(true);

            User child = mission.getChild();
            Double totalPoints = child.getTotalPoints() == null ? 0.0 : child.getTotalPoints();
            child.setTotalPoints(totalPoints + mission.getPoints());

            userRepository.save(child);
        } else {
            mission.setConfirm(false);
            mission.setCompletedAt(null);
        }

        missionRepository.save(mission);

        try {
            fcmService.sendNotificationToUser(mission.getChild().getId(), 
                "Nhiệm vụ đã được xác nhận", 
                    parent.getName() + " đã xác nhận nhiệm vụ của bạn", 
                    "https://firebasestorage.googleapis.com/v0/b/lvkmusic.appspot.com/o/xbn3urb9oc?alt=media&token=b6519c29-eb8b-4d04-aa62-134967c256ea");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

        return ResponseMessage.builder()
                .status(200)
                .message("Xác nhận nhiệm vụ thành công")
                .build();
    }

    @Override
    public Page<Mission> getMyMissions(String username, String keyword, Pageable pageable) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        return missionRepository.findByTitleContainingAndParent_UsernameOrChild_UsernameAndDeletedAtNull(
                keyword, username, username, pageable);
    }
}
