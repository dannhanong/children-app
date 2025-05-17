package com.team.child_be.services;

import java.util.List;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.team.child_be.dtos.requests.DeviceTokenRequest;
import com.team.child_be.models.DeviceToken;

public interface FCMService {
    String sendNotification(String token, String title, String body, String imageUrl) throws FirebaseMessagingException;
    String sendNotification(String token, String title, String body) throws FirebaseMessagingException;
    void sendNotificationToUser(Long userId, String title, String body, String imageUrl) throws FirebaseMessagingException;
    DeviceToken registerDeviceToken(String username, DeviceTokenRequest request);
    DeviceToken registerDeviceToken(DeviceTokenRequest request);
    void deactivateDeviceToken(String token);
    List<DeviceToken> getUserActiveTokens(Long userId);
}
