package com.team.child_be.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class ChildInfoResponse {
    Long id;
    String name;
    String phoneNumber;
    String avatarCode;
    Double totalPoints;
    Integer batteryLevel;
}
