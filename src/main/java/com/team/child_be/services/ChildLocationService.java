package com.team.child_be.services;

import java.util.List;

import com.team.child_be.dtos.requests.ChildLocationRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.ChildLocation;

public interface ChildLocationService {
    ResponseMessage addChildLocation(String username, ChildLocationRequest childLocationRequest);
    List<ChildLocation> getFamilyLocations(String username);
}
