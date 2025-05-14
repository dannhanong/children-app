package com.team.child_be.dtos.requests;

import java.time.LocalDateTime;

public record MissionRequest(
    Long childId,
    String title,
    String description,
    LocalDateTime deadline,
    Double points
) {}
