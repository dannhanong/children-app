package com.team.child_be.services;

import java.util.List;

import com.team.child_be.dtos.requests.ChatlogFolderRequest;
import com.team.child_be.dtos.requests.FolderRequest;
import com.team.child_be.dtos.responses.ChatFolderResponse;
import com.team.child_be.dtos.responses.ChatlogResponse;
import com.team.child_be.dtos.responses.ResponseMessage;

public interface ChatFolderService {
    // Quản lý thư mục
    List<ChatFolderResponse> getMyFolders(String username);
    ChatFolderResponse createFolder(String username, FolderRequest request);
    ChatFolderResponse updateFolder(String username, Long folderId, FolderRequest request);
    ResponseMessage deleteFolder(String username, Long folderId);
    
    // Quản lý tin nhắn trong thư mục
    List<ChatlogResponse> getChatlogsByFolder(String username, Long folderId, String keyword);
    ResponseMessage addChatlogToFolder(String username, ChatlogFolderRequest request);
    ResponseMessage removeChatlogFromFolder(String username, ChatlogFolderRequest request);
}