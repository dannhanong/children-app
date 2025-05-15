package com.team.child_be.services.impls;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.team.child_be.dtos.enums.ChatType;
import com.team.child_be.dtos.requests.SendMessageRequest;
import com.team.child_be.dtos.responses.ChatlogResponse;
import com.team.child_be.dtos.responses.ConversationResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.dtos.responses.UserForChatResponse;
import com.team.child_be.models.Chatlog;
import com.team.child_be.models.Conversation;
import com.team.child_be.models.User;
import com.team.child_be.repositories.ChatlogRepository;
import com.team.child_be.repositories.ConversationRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.ConversationService;
import com.team.child_be.services.FileUploadService;

@Service
public class ConversationServiceImpl implements ConversationService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ChatlogRepository chatlogRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private ChatModel chatModel;

    @Override
    public List<ConversationResponse> getMyConversations(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        List<Conversation> conversations = conversationRepository
            .findByUserOne_UsernameOrUserTwo_Username(user.getUsername(), user.getUsername());

        return conversations.stream()
            .map(conversation -> {
                if (conversation.getUserOne().getUsername().equals(user.getUsername())) {
                    return convertToConversationResponse(conversation, conversation.getUserTwo());
                } else {
                    return convertToConversationResponse(conversation, conversation.getUserOne());
                }
            })
            .toList();
    }

    @Override
    public ResponseMessage sendMessage(String username, SendMessageRequest sendMessageRequest) {
        User sender = userRepository.findByUsername(username);
        if (sender == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        User receiver = userRepository.findById(Long.valueOf(sendMessageRequest.receiverId()))
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        if (receiver.getId() == sender.getId()) {
            return ResponseMessage.builder()
                .status(400)
                .message("Không thể gửi tin nhắn cho chính mình")
                .build();
        }

        Conversation conversation = conversationRepository
            .findByUserOne_UsernameAndUserTwo_Username(sender.getUsername(), receiver.getUsername());

        if (sender.getId() == receiver.getId()) {
            return ResponseMessage.builder()
                .status(400)
                .message("Không thể gửi tin nhắn cho chính mình")
                .build();
        }

        if (userRepository.findById(receiver.getId()).get().getUsername().equals("bot@gmail.com")) {
            return sendMessageToBot(sender, receiver, sendMessageRequest.message(), conversation);
        }

        MultipartFile file = sendMessageRequest.file();

        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUserOne(sender);
            conversation.setUserTwo(receiver);
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());
            Conversation newConversation = conversationRepository.save(conversation);

            String fileCode = null;

            if (file != null) {
                try {
                    fileCode = fileUploadService.uploadFile(file).getFileCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Chatlog chatlog = Chatlog.builder()
                .sender(sender)
                .receiver(receiver)
                .conversation(newConversation)
                .type(file != null ? ChatType.IMG : ChatType.TEXT)
                .message(file != null ? fileCode : sendMessageRequest.message())
                .seen(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

            chatlogRepository.save(chatlog);
            return ResponseMessage.builder()
                .status(200)
                .message("Gửi tin nhắn thành công")
                .build();
        }

        String fileCode = null;

        if (file != null) {
            try {
                fileCode = fileUploadService.uploadFile(file).getFileCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Chatlog chatlog = Chatlog.builder()
            .sender(sender)
            .receiver(receiver)
            .conversation(conversation)
            .type(file != null ? ChatType.IMG : ChatType.TEXT)
            .message(file != null ? fileCode : sendMessageRequest.message())
            .seen(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deletedAt(null)
            .build();

        chatlogRepository.save(chatlog);

        simpMessagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/chat",
                                        new ResponseMessage(200, "success"));

        return ResponseMessage.builder()
            .status(200)
            .message("Gửi tin nhắn thành công")
            .build();
    }
    
    private ConversationResponse convertToConversationResponse(Conversation conversation, User user) {
        Chatlog lastChatlog = chatlogRepository
            .findFirstByConversationOrderByCreatedAtDesc(conversation);

        String lastMessage = lastChatlog != null ? lastChatlog.getMessage() : "";

        return ConversationResponse.builder()
            .id(conversation.getId())
            .wantToSendUser(UserForChatResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarCode(user.getAvatarCode())
                .build()
            )
            .lastMessage(lastMessage != null ? lastMessage : "")
            .lastMessageTime(conversation.getCreatedAt())
            .build();
    }

    @Override
    public List<ChatlogResponse> geChatlogs(String username, Long conversationId) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        if (conversation.getUserOne().getId() != user.getId() && conversation.getUserTwo().getId() != user.getId()) {
            throw new RuntimeException("Bạn không có quyền truy cập vào cuộc trò chuyện này");
        }

        List<Chatlog> chatlogs = chatlogRepository.findByConversation(conversation);

        for (Chatlog chatlog : chatlogs) {
            chatlog.setSeen(true);
            chatlogRepository.save(chatlog);
        }

        return chatlogs.stream()
            .map(chatlog -> convertToChatlogResponse(chatlog))
            .toList();
    }

    private ChatlogResponse convertToChatlogResponse(Chatlog chatlog) {
        return ChatlogResponse.builder()
            .id(chatlog.getId())
            .sender(UserForChatResponse.builder()
                .id(chatlog.getSender().getId())
                .name(chatlog.getSender().getName())
                .avatarCode(chatlog.getSender().getAvatarCode())
                .build()
            )
            .receiver(UserForChatResponse.builder()
                .id(chatlog.getReceiver().getId())
                .name(chatlog.getReceiver().getName())
                .avatarCode(chatlog.getReceiver().getAvatarCode())
                .build()
            )
            .message(chatlog.getMessage() != null ? chatlog.getMessage() : "")
            .type(chatlog.getType())
            .seen(chatlog.isSeen())
            .build();
    }

    @Override
    public void createDefaultConversation(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Conversation conversation = new Conversation();
        conversation.setUserOne(user);
        conversation.setUserTwo(userRepository.findByUsername("bot@gmail.com"));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(conversation);
    }

    private ResponseMessage sendMessageToBot(User sender, User receiver, String message, Conversation conversation) {
        Chatlog chatlog1 = Chatlog.builder()
            .sender(sender)
            .receiver(receiver)
            .conversation(conversation)
            .message(message)
            .type(ChatType.TEXT)
            .seen(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        List<Chatlog> chatlogs = chatlogRepository.findByConversation(conversation);
        String chatLogTexts = getChatLogTexts(chatlogs);
        Prompt questionPrompt = getQuestionPrompt(message, chatLogTexts);

        chatlogRepository.save(chatlog1);

        ChatResponse res = chatModel.call(questionPrompt);
        
        String answer = res.getResult().getOutput().getText();

        Chatlog chatlog2 = Chatlog.builder()
            .sender(receiver)
            .receiver(sender)
            .conversation(conversation)
            .message(answer)
            .type(ChatType.TEXT)
            .seen(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        chatlogRepository.save(chatlog2);
        simpMessagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/chat",
                                        new ResponseMessage(200, "success"));
        return ResponseMessage.builder()
            .status(200)
            .message("Gửi tin nhắn thành công")
            .build();
    }

    private String getChatLogTexts(List<Chatlog> chatLogs) {
        final String BOT_USERNAME = "bot@gmail.com";
        StringBuilder chatLogTexts = new StringBuilder();

        chatLogs = chatLogs.stream()
                .sorted(Comparator.comparing(Chatlog::getCreatedAt))
                .toList();
        
        for (Chatlog chatLog : chatLogs) {
            String senderUsername = chatLog.getSender().getUsername();
            String message = chatLog.getMessage() != null ? chatLog.getMessage() : "";
            
            if (BOT_USERNAME.equals(senderUsername)) {
                chatLogTexts.append("bot message: ").append(message).append("\n");
            } else {
                chatLogTexts.append("user message: ").append("\"")
                        .append(chatLog.getSender().getName()).append("\" ")
                        .append(message).append("\n");
            }
            
            chatLogTexts.append("\n");
        }
        
        return chatLogTexts.toString();
    }

    private Prompt getQuestionPrompt(String question, String chatLogTexts) {
        String prompt = "Bạn là một trợ lý ảo. Bạn có thể trả lời các câu hỏi của người dùng. Đây là lịch sử trò chuyện: " +
                chatLogTexts + "\n" +
                "Người dùng hỏi: " + question + "\n" +
                "Trợ lý ảo trả lời: ";
        return new Prompt(prompt);
    }
}
