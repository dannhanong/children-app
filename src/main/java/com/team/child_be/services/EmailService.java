package com.team.child_be.services;

import com.team.child_be.dtos.responses.NotificationEvent;

public interface EmailService {
    void sendForgotPasswordEmail(NotificationEvent notificationEvent);
}
