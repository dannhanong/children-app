package com.team.child_be.dtos.requests;

public record CallRequest(
    Long receiverId,
    String channelName
) {}