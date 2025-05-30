package com.team.child_be.services.impls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.team.child_be.dtos.requests.DeviceTokenRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.DeviceToken;
import com.team.child_be.models.User;
import com.team.child_be.repositories.DeviceTokenRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.FCMService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FCMServiceImpl implements FCMService{
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public String sendNotification(String token, String title, String body, String imageUrl) throws FirebaseMessagingException {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(title)
                .setBody(body);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            notificationBuilder.setImage(imageUrl);
        }

        Notification notification = notificationBuilder.build();
        
        Message message = Message.builder()
                .setNotification(notification)
                .putData("type", "NOTIFICATION_TYPE")
                .putData("id", "notification-id")
                .setToken(token)
                .build();
        
        try {
            String result = firebaseMessaging.send(message);

            deviceTokenRepository.findByTokenContaining(token.split(":")[0]).ifPresent(deviceToken -> {
                deviceToken.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(deviceToken);
            });

            return result;
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() != null) {
                switch (e.getMessagingErrorCode()) {
                    case UNREGISTERED:
                        // Token không còn hợp lệ (thiết bị đã gỡ cài đặt ứng dụng)
                        log.warn("Token is no longer valid: {}", token);
                        break;
                    case INVALID_ARGUMENT:
                        log.error("Invalid token format: {}", token);
                        break;
                    default:
                        log.error("Failed to send notification: {}", e.getMessage());
                }
            }
            throw e;
        }
    }

    @Override
    public void sendNotificationToUser(Long userId, String title, String body, String imageUrl) throws FirebaseMessagingException {
        DeviceToken token = deviceTokenRepository.findTopByUser_IdAndActiveTrueOrderByIdDesc(userId);
        if (token == null) {
            return;
        }

        try {
            sendNotification(token.getToken(), title, body, imageUrl);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public DeviceToken registerDeviceToken(String username, DeviceTokenRequest request) {
        User user = userRepository.findByUsername(username);
        
        // deviceTokenRepository.findByTokenContaining(request.token().split(":")[0])
        //     .ifPresent(existingToken -> {
        //         if (existingToken.getUser().getId() != user.getId()) {
        //             existingToken.setActive(false);
        //             deviceTokenRepository.save(existingToken);
        //         }
        //     });

        DeviceToken deviceToken = deviceTokenRepository.findByTokenContaining(request.token().split(":")[0])
            .map(token -> {
                token.setUser(user);
                token.setDeviceName(request.deviceName());
                token.setDeviceModel(request.deviceModel());
                token.setActive(true);
                token.setUpdatedAt(LocalDateTime.now());
                token.setLastUsedAt(LocalDateTime.now());
                return token;
            })
            .orElseGet(() -> DeviceToken.builder()
                .user(user)
                .token(request.token())
                .deviceName(request.deviceName())
                .deviceModel(request.deviceModel())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .build());

        return deviceTokenRepository.save(deviceToken);
    }

    @Override
    @Transactional
    public void deactivateDeviceToken(String username, String token) {
        log.info("Deactivating device token: {}", token);

        deviceTokenRepository.findByTokenContainingAndUser_Username(token.split(":")[0], username)
                .ifPresent(deviceToken -> {
                    deviceToken.setActive(false);
                    deviceToken.setUpdatedAt(LocalDateTime.now());
                    deviceTokenRepository.save(deviceToken);
                });
    }

    @Override
    public List<DeviceToken> getUserActiveTokens(Long userId) {
        log.info("Fetching active tokens for user ID: {}", userId);
    
        return deviceTokenRepository.findByUser_IdAndActiveTrue(userId);
    }

    // @Override
    // public String sendNotification(String token, String title, String body) throws FirebaseMessagingException {
    //     return sendNotification(token, title, body, null);
    // }

    @Override
    public ResponseMessage sendSosNotification(String username) throws FirebaseMessagingException {
        User child = userRepository.findByUsername(username);
        if (child == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        Long parentId = child.getParentId();

        String title = "Cảnh báo SOS!";
        String body =  child.getName() + " đã gửi cảnh báo SOS!";

        List<DeviceToken> tokens = deviceTokenRepository.findByUser_IdAndActiveTrue(parentId);
        if (tokens.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thiết bị nào của người dùng");
        }

        for (DeviceToken deviceToken : tokens) {
            sendNotification(deviceToken.getToken(), title, body, null);
        }

        return new ResponseMessage(200, "Đã gửi thông báo SOS thành công");
    }

    @Override
    public String sendNotificationWithData(String token, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(title)
                .setBody(body);

        Message.Builder messageBuilder = Message.builder()
                .setNotification(notificationBuilder.build())
                .setToken(token);
                
        // Thêm tất cả dữ liệu tùy chỉnh
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                messageBuilder.putData(entry.getKey(), entry.getValue());
            }
        }
        
        Message message = messageBuilder.build();
        
        try {
            String result = firebaseMessaging.send(message);

            deviceTokenRepository.findByTokenContaining(token.split(":")[0]).ifPresent(deviceToken -> {
                deviceToken.setLastUsedAt(LocalDateTime.now());
                deviceTokenRepository.save(deviceToken);
            });

            return result;
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() != null) {
                switch (e.getMessagingErrorCode()) {
                    case UNREGISTERED:
                        log.warn("Token is no longer valid: {}", token);
                        break;
                    case INVALID_ARGUMENT:
                        log.error("Invalid token format: {}", token);
                        break;
                    default:
                        log.error("Failed to send notification: {}", e.getMessage());
                }
            }
            throw e;
        }
    }

    @Override
    public void sendNotificationWithDataToUser(Long userId, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {
        List<DeviceToken> tokens = deviceTokenRepository.findByUser_IdAndActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.warn("No active device tokens found for user ID: {}", userId);
            return;
        }

        for (DeviceToken deviceToken : tokens) {
            try {
                sendNotificationWithData(deviceToken.getToken(), title, body, data);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
            }
        }
    }
    
}
