package com.team.child_be.dtos.responses;

import java.util.List;

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
public class ChatFolderResponse {
    private Long id;
    private String name;
    private int messageCount;
    private List<ChatlogResponse> messages;
}