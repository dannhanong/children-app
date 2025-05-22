package com.team.child_be.services.impls;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team.child_be.dtos.requests.ChatlogFolderRequest;
import com.team.child_be.dtos.requests.FolderRequest;
import com.team.child_be.dtos.responses.ChatFolderResponse;
import com.team.child_be.dtos.responses.ChatlogResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.dtos.responses.UserForChatResponse;
import com.team.child_be.models.ChatFolder;
import com.team.child_be.models.Chatlog;
import com.team.child_be.models.User;
import com.team.child_be.repositories.ChatFolderRepository;
import com.team.child_be.repositories.ChatlogRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.ChatFolderService;

@Service
public class ChatFolderServiceImpl implements ChatFolderService {
    @Autowired
    private ChatFolderRepository chatFolderRepository;
    
    @Autowired
    private ChatlogRepository chatlogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatFolderResponse> getMyFolders(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        List<ChatFolder> folders = chatFolderRepository.findByUser_Username(username);
        
        return folders.stream()
                .map(folder -> {
                    int messageCount = folder.getChatlogs().size();
                    return ChatFolderResponse.builder()
                            .id(folder.getId())
                            .name(folder.getName())
                            .messageCount(messageCount)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ChatFolderResponse createFolder(String username, FolderRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        // Kiểm tra tên thư mục đã tồn tại chưa
        if (chatFolderRepository.existsByNameAndUser_Username(request.name(), username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thư mục đã tồn tại");
        }
        
        ChatFolder folder = ChatFolder.builder()
                .name(request.name())
                .user(user)
                .chatlogs(new HashSet<>())
                .build();
        
        folder = chatFolderRepository.save(folder);
        
        return ChatFolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .messageCount(0)
                .build();
    }
    
    @Override
    @Transactional
    public ChatFolderResponse updateFolder(String username, Long folderId, FolderRequest request) {
        ChatFolder folder = chatFolderRepository.findByIdAndUser_Username(folderId, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));

        // Kiểm tra tên thư mục mới đã tồn tại chưa
        if (!folder.getName().equals(request.name()) && 
            chatFolderRepository.existsByNameAndUser_Username(request.name(), username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thư mục đã tồn tại");
        }

        folder.setName(request.name());
        folder = chatFolderRepository.save(folder);
        
        return ChatFolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .messageCount(folder.getChatlogs().size())
                .build();
    }
    
    @Override
    @Transactional
    public ResponseMessage deleteFolder(String username, Long folderId) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        ChatFolder folder = chatFolderRepository.findByIdAndUser_Username(folderId, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));

        // Xóa liên kết với chatlogs trước
        for (Chatlog chatlog : folder.getChatlogs()) {
            chatlog.getFolders().remove(folder);
            chatlogRepository.save(chatlog);
        }
        
        chatFolderRepository.delete(folder);
        return new ResponseMessage(200, "Xóa thư mục thành công");
    }
    
    @Override
    public List<ChatlogResponse> getChatlogsByFolder(String username, Long folderId, String keyword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        List<Chatlog> chatlogs = chatlogRepository.findByFolderAndUser(folderId, user, keyword);

        return chatlogs.stream()
                .map(chatlog -> convertToChatlogResponse(chatlog, user))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ResponseMessage addChatlogToFolder(String username, ChatlogFolderRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        ChatFolder folder = chatFolderRepository.findByIdAndUser_Username(request.folderId(), username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));
        Chatlog chatlog = chatlogRepository.findByIdAndSenderOrIdAndReceiver(request.chatlogId(), user, request.chatlogId(), user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tin nhắn"));

        // Kiểm tra xem tin nhắn đã thuộc thư mục chưa
        if (chatlogRepository.existsByIdAndFoldersContaining(chatlog.getId(), folder)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tin nhắn đã có trong thư mục");
        }
        
        chatlog.getFolders().add(folder);
        chatlogRepository.save(chatlog);

        return new ResponseMessage(200, "Đã thêm tin nhắn vào thư mục");
    }
    
    @Override
    @Transactional
    public ResponseMessage removeChatlogFromFolder(String username, ChatlogFolderRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        ChatFolder folder = chatFolderRepository.findByIdAndUser_Username(request.folderId(), username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));
        Chatlog chatlog = chatlogRepository.findByIdAndSenderOrIdAndReceiver(request.chatlogId(), user, request.chatlogId(), user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tin nhắn"));

        // Kiểm tra xem tin nhắn có thuộc thư mục không
        if (!chatlogRepository.existsByIdAndFoldersContaining(chatlog.getId(), folder)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tin nhắn không có trong thư mục");
        }
        
        chatlog.getFolders().remove(folder);
        chatlogRepository.save(chatlog);

        return new ResponseMessage(200, "Đã xóa tin nhắn khỏi thư mục");
    }
    
    private ChatlogResponse convertToChatlogResponse(Chatlog chatlog, User user) {
        if (chatlog.getSender().getId() == user.getId()) {
            return ChatlogResponse.builder()
                .id(chatlog.getId())
                .sender(UserForChatResponse.builder()
                    .id(chatlog.getSender().getId())
                    .name(chatlog.getSender().getName())
                    .avatarCode(chatlog.getSender().getAvatarCode())
                    .build()
                )
                .message(chatlog.getMessage() != null ? chatlog.getMessage() : "")
                .type(chatlog.getType())
                .seen(chatlog.isSeen())
                .build();
        } else {
            return ChatlogResponse.builder()
                .id(chatlog.getId())
                .receiver(UserForChatResponse.builder()
                    .id(chatlog.getSender().getId())
                    .name(chatlog.getSender().getName())
                    .avatarCode(chatlog.getSender().getAvatarCode())
                    .build()
                )
                .message(chatlog.getMessage() != null ? chatlog.getMessage() : "")
                .type(chatlog.getType())
                .seen(chatlog.isSeen())
                .build();
        }
    }
}