package com.team.child_be.services.impls;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.team.child_be.configs.AgoraConfig;
import com.team.child_be.dtos.requests.CallRequest;
import com.team.child_be.dtos.responses.AgoraTokenResponse;
import com.team.child_be.dtos.responses.CallResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.dtos.responses.UserForChatResponse;
import com.team.child_be.models.Call;
import com.team.child_be.models.User;
import com.team.child_be.repositories.CallRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.AgoraService;
import com.team.child_be.services.FCMService;

import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AgoraServiceImpl implements AgoraService {

    @Autowired
    private AgoraConfig agoraConfig;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CallRepository callRepository;
    
    @Autowired
    private FCMService fcmService;

    @Override
    public AgoraTokenResponse generateToken(String channelName) {
        if (channelName == null || channelName.isEmpty()) {
            channelName = UUID.randomUUID().toString();
        }
        
        // Tạo token với uid là 0 (để cho phép bất kỳ uid nào)
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000);
        int expireTime = timestamp + agoraConfig.getTokenExpireInSeconds();
        
        String token = tokenBuilder.buildTokenWithUid(
            agoraConfig.getAppId(),
            agoraConfig.getAppCertificate(),
            channelName,
            0, // uid = 0 cho bất kỳ người dùng nào
            Role.Role_Publisher,
            expireTime
        );
        
        return AgoraTokenResponse.builder()
            .token(token)
            .channelName(channelName)
            .uid(0)
            .expireTime(expireTime)
            .build();
    }

    @Override
    public ResponseMessage initiateCall(String username, CallRequest callRequest) {
        User caller = userRepository.findByUsername(username);
        if (caller == null) {
            return ResponseMessage.builder()
                .status(404)
                .message("Không tìm thấy người dùng")
                .build();
        }
        
        User receiver = userRepository.findById(callRequest.receiverId())
            .orElse(null);
        if (receiver == null) {
            return ResponseMessage.builder()
                .status(404)
                .message("Không tìm thấy người nhận cuộc gọi")
                .build();
        }
        
        String channelName = callRequest.channelName();
        if (channelName == null || channelName.isEmpty()) {
            channelName = UUID.randomUUID().toString();
        }
        
        Call call = Call.builder()
            .caller(caller)
            .receiver(receiver)
            .channelName(channelName)
            .startTime(LocalDateTime.now())
            .answered(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        callRepository.save(call);
        
        // Gửi thông báo đến người nhận qua FCM với dữ liệu bổ sung cho cuộc gọi
        try {
            Map<String, String> callData = new HashMap<>();
            callData.put("type", "call");
            callData.put("channelId", channelName);
            callData.put("callerId", caller.getId().toString());
            callData.put("callerName", caller.getName());
            
            fcmService.sendNotificationWithDataToUser(
                receiver.getId(),
                "Cuộc gọi đến",
                "Cuộc gọi từ " + caller.getName(),
                callData
            );
            
            log.info("Call notification sent successfully to user ID: {} with channel: {}", receiver.getId(), channelName);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send call notification: {}", e.getMessage());
            // Tiếp tục xử lý, không throw exception
        }
        
        return ResponseMessage.builder()
            .status(200)
            .message(channelName)
            .build();
    }

    @Override
    public ResponseMessage endCall(String username, String channelName) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseMessage.builder()
                .status(404)
                .message("Không tìm thấy người dùng")
                .build();
        }
        
        Call call = callRepository.findByChannelName(channelName)
            .orElse(null);
        if (call == null) {
            return ResponseMessage.builder()
                .status(404)
                .message("Không tìm thấy cuộc gọi")
                .build();
        }
        
        // Chỉ cho phép người gọi hoặc người nhận kết thúc cuộc gọi
        if (!call.getCaller().getId().equals(user.getId()) && !call.getReceiver().getId().equals(user.getId())) {
            return ResponseMessage.builder()
                .status(403)
                .message("Bạn không có quyền kết thúc cuộc gọi này")
                .build();
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        call.setEndTime(endTime);
        
        // Tính toán thời lượng cuộc gọi
        if (call.getStartTime() != null) {
            int durationInSeconds = (int) ChronoUnit.SECONDS.between(call.getStartTime(), endTime);
            call.setDurationInSeconds(durationInSeconds);
        }
        
        // Nếu cuộc gọi được trả lời, đánh dấu là answered = true
        if (call.getEndTime() != null && call.getStartTime() != null) {
            call.setAnswered(true);
        }
        
        call.setUpdatedAt(LocalDateTime.now());
        callRepository.save(call);
        
        return ResponseMessage.builder()
            .status(200)
            .message("Đã kết thúc cuộc gọi")
            .build();
    }

    @Override
    public List<CallResponse> getCallHistory(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        
        List<Call> calls = callRepository.findCallHistoryByUserId(user.getId());
        
        return calls.stream()
            .map(call -> CallResponse.builder()
                .id(call.getId())
                .caller(UserForChatResponse.builder()
                    .id(call.getCaller().getId())
                    .name(call.getCaller().getName())
                    .avatarCode(call.getCaller().getAvatarCode())
                    .build())
                .receiver(UserForChatResponse.builder()
                    .id(call.getReceiver().getId())
                    .name(call.getReceiver().getName())
                    .avatarCode(call.getReceiver().getAvatarCode())
                    .build())
                .channelName(call.getChannelName())
                .startTime(call.getStartTime())
                .endTime(call.getEndTime())
                .durationInSeconds(call.getDurationInSeconds())
                .answered(call.getAnswered())
                .build())
            .toList();
    }

    @Override
    public Map<String, Object> initiateCallMerge(String username, CallRequest callRequest) {
        User caller = userRepository.findByUsername(username);
        if (caller == null) {
            return Map.of(
                "status", 404,
                "message", "Không tìm thấy người dùng"
            );
        }
        
        User receiver = userRepository.findById(callRequest.receiverId())
        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người nhận"));
    
        // Tạo channelName ngẫu nhiên
        String channelName = UUID.randomUUID().toString();
        
        // Tạo cuộc gọi trong database
        Call call = Call.builder()
            .caller(caller)
            .receiver(receiver)
            .channelName(channelName)
            .startTime(LocalDateTime.now())
            .answered(false)
            .build();
        
        callRepository.save(call);
        
        // Tạo Agora token cho người gọi
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        int expirationTimeInSeconds = 3600; // 1 giờ
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        
        String token = tokenBuilder.buildTokenWithUid(
            agoraConfig.getAppId(),
            agoraConfig.getAppCertificate(),
            channelName,
            0, // uid
            RtcTokenBuilder.Role.Role_Publisher,
            timestamp
        );
        
        // Gửi thông báo FCM cho người nhận
        try {
            Map<String, String> callData = new HashMap<>();
            callData.put("type", "call");
            callData.put("channelId", channelName);
            callData.put("callerId", caller.getId().toString());
            callData.put("callerName", caller.getName());
            
            fcmService.sendNotificationWithDataToUser(
                receiver.getId(),
                "Cuộc gọi đến",
                "Cuộc gọi từ " + caller.getName(),
                callData
            );
        } catch (Exception e) {
            log.error("Error sending FCM notification", e);
            // Tiếp tục xử lý, không throw exception
        }
        
        // Trả về đầy đủ thông tin cần thiết
        return Map.of(
            "status", 200,
            "channelName", channelName,
            "token", token,
            "uid", 0,
            "expireTime", timestamp
        );
    }

    @Override
    public Map<String, Object> joinCall(String username, String channelName) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Map.of(
                "status", 404,
                "message", "Không tìm thấy người dùng"
            );
        }
        
        Call call = callRepository.findByChannelName(channelName)
        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cuộc gọi với kênh: " + channelName));
    
        // Kiểm tra quyền tham gia cuộc gọi
        boolean isParticipant = call.getCaller().getId().equals(user.getId()) || 
                            call.getReceiver().getId().equals(user.getId());
        
        if (!isParticipant) {
            return Map.of(
                "status", 403,
                "message", "Bạn không có quyền tham gia cuộc gọi này"
            );
        }
        
        // Tạo Agora token cho người tham gia
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        int expirationTimeInSeconds = 3600; // 1 giờ
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        
        String token = tokenBuilder.buildTokenWithUid(
            agoraConfig.getAppId(),
            agoraConfig.getAppCertificate(),
            channelName,
            0, // uid
            RtcTokenBuilder.Role.Role_Publisher,
            timestamp
        );
        
        // Nếu là người nhận, đánh dấu cuộc gọi đã được trả lời
        if (call.getReceiver().getId().equals(user.getId())) {
            call.setAnswered(true);
            callRepository.save(call);
        }
        
        return Map.of(
            "status", 200,
            "token", token,
            "channelName", channelName,
            "uid", 0,
            "expireTime", timestamp
        );
    }
}