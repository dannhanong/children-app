package com.team.child_be.services;

import java.util.List;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.team.child_be.dtos.requests.DeviceTokenRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.DeviceToken;

public interface FCMService {
    String sendNotification(String token, String title, String body, String imageUrl) throws FirebaseMessagingException;
    // String sendNotification(String token, String title, String body) throws FirebaseMessagingException;
    void sendNotificationToUser(Long userId, String title, String body, String imageUrl) throws FirebaseMessagingException;
    DeviceToken registerDeviceToken(String username, DeviceTokenRequest request);
    void deactivateDeviceToken(String username, String token);
    List<DeviceToken> getUserActiveTokens(Long userId);
    ResponseMessage sendSosNotification(String username) throws FirebaseMessagingException;
    String sendNotificationWithData(String token, String title, String body, java.util.Map<String, String> data) throws FirebaseMessagingException;
    void sendNotificationWithDataToUser(Long userId, String title, String body, java.util.Map<String, String> data) throws FirebaseMessagingException;
}
