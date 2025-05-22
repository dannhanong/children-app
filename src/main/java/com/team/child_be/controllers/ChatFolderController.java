package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.ChatlogFolderRequest;
import com.team.child_be.dtos.requests.FolderRequest;
import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.ChatFolderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/folders")
public class ChatFolderController {
    @Autowired
    private ChatFolderService chatFolderService;
    @Autowired
    private JwtService jwtService;
    
    @GetMapping("/my-folders")
    public ResponseEntity<?> getMyFolders(HttpServletRequest request) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.getMyFolders(username));
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(HttpServletRequest request, 
                                         @Valid @RequestBody FolderRequest folderRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.createFolder(username, folderRequest));
    }
    
    @PutMapping("/update/{folderId}")
    public ResponseEntity<?> updateFolder(HttpServletRequest request, 
                                         @PathVariable Long folderId,
                                         @Valid @RequestBody FolderRequest folderRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.updateFolder(username, folderId, folderRequest));
    }
    
    @DeleteMapping("/delete/{folderId}")
    public ResponseEntity<?> deleteFolder(HttpServletRequest request, 
                                         @PathVariable Long folderId) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.deleteFolder(username, folderId));
    }
    
    @GetMapping("/{folderId}/messages")
    public ResponseEntity<?> getMessagesInFolder(HttpServletRequest request, 
                                               @PathVariable Long folderId,
                                               @RequestParam(defaultValue = "") String keyword) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.getChatlogsByFolder(username, folderId, keyword));
    }
    
    @PostMapping("/add-message")
    public ResponseEntity<?> addMessageToFolder(HttpServletRequest request, 
                                              @Valid @RequestBody ChatlogFolderRequest chatlogFolderRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.addChatlogToFolder(username, chatlogFolderRequest));
    }
    
    @PostMapping("/remove-message")
    public ResponseEntity<?> removeMessageFromFolder(HttpServletRequest request, 
                                                   @Valid @RequestBody ChatlogFolderRequest chatlogFolderRequest) {
        String username = jwtService.getUsernameFromRequest(request);
        return ResponseEntity.ok(chatFolderService.removeChatlogFromFolder(username, chatlogFolderRequest));
    }
}