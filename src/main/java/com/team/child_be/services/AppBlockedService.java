package com.team.child_be.services;

import java.util.List;

import com.team.child_be.dtos.requests.AppBlockedRequest;
import com.team.child_be.dtos.responses.ResponseMessage;

public interface AppBlockedService {
    ResponseMessage addAppBlocked(String username, AppBlockedRequest appBlockedRequest);
    ResponseMessage addWebBlocked(String username, AppBlockedRequest appBlockedRequest);
    ResponseMessage deleteAppBlocked(String username, Long id);
    List<String> getMyChildAppBlocked(String username, Long childId);
    List<String> getMyChildWebBlocked(String username, Long childId);
}
