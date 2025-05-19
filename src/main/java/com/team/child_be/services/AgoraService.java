package com.team.child_be.services;

import com.team.child_be.dtos.requests.CallRequest;
import com.team.child_be.dtos.responses.AgoraTokenResponse;
import com.team.child_be.dtos.responses.CallResponse;
import com.team.child_be.dtos.responses.ResponseMessage;

import java.util.List;

public interface AgoraService {
    AgoraTokenResponse generateToken(String channelName);
    
    ResponseMessage initiateCall(String username, CallRequest callRequest);
    ResponseMessage endCall(String username, String channelName);

    List<CallResponse> getCallHistory(String username);
}