package com.team.child_be.services;

import com.team.child_be.dtos.requests.CallRequest;
import com.team.child_be.dtos.responses.AgoraTokenResponse;
import com.team.child_be.dtos.responses.CallResponse;
import com.team.child_be.dtos.responses.ResponseMessage;

import java.util.List;
import java.util.Map;

public interface AgoraService {
    AgoraTokenResponse generateToken(String channelName);
    
    ResponseMessage initiateCall(String username, CallRequest callRequest);

    Map<String, Object> initiateCallMerge(String username, CallRequest callRequest);

    ResponseMessage endCall(String username, String channelName);

    List<CallResponse> getCallHistory(String username);

    Map<String, Object> joinCall(String username, String channelName);
}