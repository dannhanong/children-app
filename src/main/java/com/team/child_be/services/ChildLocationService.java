package com.team.child_be.services;

import com.team.child_be.dtos.requests.ChildLocationRequest;
import com.team.child_be.dtos.responses.ResponseMessage;

public interface ChildLocationService {
    ResponseMessage addChildLocation(String username, ChildLocationRequest childLocationRequest);
}
