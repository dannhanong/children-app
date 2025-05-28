package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.SendMessageRequest;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.ConversationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private JwtService jwtService;

    @GetMapping("/my-conversations")
    public ResponseEntity<?> getMyConversations(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(conversationService.getMyConversations(username));
    }

    @GetMapping("/chatlogs/{conversationId}")
    public ResponseEntity<?> getChatlogs(HttpServletRequest request, @PathVariable Long conversationId) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(conversationService.geChatlogs(username, conversationId));
    }

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(HttpServletRequest request, 
                                        @Valid @ModelAttribute SendMessageRequest sendMessageRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(conversationService.sendMessage(username, sendMessageRequest));
    }

    @PutMapping("/save/{chatLogId}")
    public ResponseEntity<?> saveChatlog(HttpServletRequest request, @PathVariable Long chatLogId) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(conversationService.saveChatlog(username, chatLogId));
    }

    @GetMapping("/get/{userWantToSendId}")
    public ResponseEntity<?> getConversationByUserWantToSend(HttpServletRequest request, 
                                                              @PathVariable Long userWantToSendId) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(conversationService.getConversationByUserWantToSend(username, userWantToSendId));
    }
}
