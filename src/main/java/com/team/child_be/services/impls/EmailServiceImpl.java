package com.team.child_be.services.impls;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.team.child_be.dtos.responses.NotificationEvent;
import com.team.child_be.services.EmailService;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService{
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendForgotPasswordEmail(NotificationEvent notificationEvent) {
        String toAddress = notificationEvent.getRecipient();
        String subject = "Quên mật khẩu";
        String senderName = "FATS";

        Map<String, Object> model = new HashMap<>();
        model.put("name", notificationEvent.getNameOfRecipient());
        model.put("newPassword", notificationEvent.getBody());

        Context context = new Context();
        context.setVariables(model);
        String content = templateEngine.process("forgot-password-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom("CHILD_APP", senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
