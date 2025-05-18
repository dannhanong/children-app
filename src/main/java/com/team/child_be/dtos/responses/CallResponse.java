package com.team.child_be.dtos.responses;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallResponse {
    Long id;
    UserForChatResponse caller;
    UserForChatResponse receiver;
    String channelName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Integer durationInSeconds;
    Boolean answered;
}
