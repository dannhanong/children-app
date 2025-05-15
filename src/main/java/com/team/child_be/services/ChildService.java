package com.team.child_be.services;

import com.team.child_be.dtos.requests.ChildRequest;
import com.team.child_be.dtos.responses.ChildResponse;
import com.team.child_be.dtos.responses.ResponseMessage;

import java.util.List;

public interface ChildService {
    

    ChildResponse createChild(ChildRequest childRequest, String username);
    

    ChildResponse updateChild(Long id, ChildRequest childRequest, String username);
    

    ResponseMessage deleteChild(Long id, String username);
    

    ChildResponse regenerateAccessCode(Long id, String username);
    

    List<ChildResponse> getAllChildren(String username);
    

    ChildResponse getChildById(Long id, String username);
    

    List<ChildResponse> searchChildren(String keyword, String username);
}
