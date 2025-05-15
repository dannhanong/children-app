package com.team.child_be.dtos.responses;

import com.team.child_be.dtos.enums.ChatType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatlogResponse {
    private Long id;
    private UserForChatResponse sender;
    private UserForChatResponse receiver;
    String message;
    ChatType type;
    private boolean seen;
}
