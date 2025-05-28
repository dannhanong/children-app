package com.team.child_be.services;

import java.util.List;

import com.team.child_be.dtos.requests.SendMessageRequest;
import com.team.child_be.dtos.responses.ChatlogResponse;
import com.team.child_be.dtos.responses.ConversationResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.Chatlog;

public interface ConversationService {
    List<ConversationResponse> getMyConversations(String username);
    ResponseMessage sendMessage(String username, SendMessageRequest sendMessageRequest);
    List<ChatlogResponse> geChatlogs(String username, Long conversationId);
    void createDefaultConversation(Long userId);
    ResponseMessage saveChatlog(String username, Long chatLogId);
    List<Chatlog> getConversationByUserWantToSend(String username, Long userWantToSendId);
}
