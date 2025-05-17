package com.team.child_be.dtos.requests;

public record AppBlockedRequest(
    String appName,
    Long childId
) {}
