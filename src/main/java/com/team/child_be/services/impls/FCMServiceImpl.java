package com.team.child_be.services.impls;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.team.child_be.dtos.requests.DeviceTokenRequest;
import com.team.child_be.models.DeviceToken;
import com.team.child_be.models.User;
import com.team.child_be.repositories.DeviceTokenRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.FCMService;

import lombok.extern.slf4j.Slf4j;

// @Service
@Slf4j
public class FCMServiceImpl implements FCMService{
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public String sendNotification(String token, String title, String body) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
        
        Message message = Message.builder()
                .setNotification(notification)
                .setToken(token)
                .build();
        
        try {
            String result = firebaseMessaging.send(message);

            deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
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
                        deactivateDeviceToken(token);
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
    public void sendNotificationToUser(Long userId, String title, String body) throws FirebaseMessagingException {
        List<DeviceToken> tokens = deviceTokenRepository.findByUser_IdAndActiveTrue(userId);

        if (tokens.isEmpty()) {
            return;
        }

        for (DeviceToken deviceToken : tokens) {
            try {
                sendNotification(deviceToken.getToken(), title, body);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    public DeviceToken registerDeviceToken(String username, DeviceTokenRequest request) {
        User user = userRepository.findByUsername(username);
        
        deviceTokenRepository.findByToken(request.token())
            .ifPresent(existingToken -> {
                if (existingToken.getUser().getId() != user.getId()) {
                    existingToken.setActive(false);
                    deviceTokenRepository.save(existingToken);
                }
            });

        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.token())
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
    public void deactivateDeviceToken(String token) {
        log.info("Deactivating device token: {}", token);
        
        deviceTokenRepository.findByToken(token)
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
    
}
