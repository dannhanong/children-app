package com.team.child_be.services;

import java.util.List;

import com.team.child_be.dtos.requests.AppBlockedRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.AppBlocked;

public interface AppBlockedService {
    ResponseMessage addAppBlocked(String username, AppBlockedRequest appBlockedRequest);
    ResponseMessage addWebBlocked(String username, AppBlockedRequest appBlockedRequest);
    ResponseMessage deleteAppBlocked(String username, Long id);
    List<AppBlocked> getMyChildAppBlocked(String username, Long childId);
    List<AppBlocked> getMyChildWebBlocked(String username, Long childId);
    List<AppBlocked> getAllAppBlocked(String username);
    List<AppBlocked> getAllWebBlocked(String username);
    List<AppBlocked> getAllAppBlockedByChild(String username);
    List<AppBlocked> getAllWebBlockedByChild(String username);
}
