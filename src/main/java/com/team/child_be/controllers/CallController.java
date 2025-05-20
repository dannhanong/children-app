package com.team.child_be.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.dtos.requests.CallRequest;
import com.team.child_be.dtos.responses.AgoraTokenResponse;
import com.team.child_be.dtos.responses.CallResponse;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.services.AgoraService;

@RestController
@RequestMapping("/call")
public class CallController {

    @Autowired
    private AgoraService agoraService;
    
    @GetMapping("/get-token")
    public ResponseEntity<AgoraTokenResponse> getToken(@RequestParam(required = false) String channelName) {
        AgoraTokenResponse token = agoraService.generateToken(channelName);
        return ResponseEntity.ok(token);
    }
    
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiateCall(
            Authentication authentication,
            @RequestBody CallRequest callRequest) {
        String username = authentication.getName();
        Map<String, Object> response = agoraService.initiateCallMerge(username, callRequest);
        return ResponseEntity.status((Integer) response.get("status")).body(response);
    }

    @GetMapping("/join")
    public ResponseEntity<Map<String, Object>> joinCall(
            Authentication authentication,
            @RequestParam String channelName) {
        String username = authentication.getName();
        Map<String, Object> response = agoraService.joinCall(username, channelName);
        return ResponseEntity.status((Integer) response.get("status")).body(response);
    }
    
    @PostMapping("/end")
    public ResponseEntity<ResponseMessage> endCall(
            Authentication authentication,
            @RequestParam String channelName) {
        String username = authentication.getName();
        ResponseMessage response = agoraService.endCall(username, channelName);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<CallResponse>> getCallHistory(Authentication authentication) {
        String username = authentication.getName();
        List<CallResponse> history = agoraService.getCallHistory(username);
        return ResponseEntity.ok(history);
    }
}