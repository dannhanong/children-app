package com.team.child_be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.Chatlog;
import com.team.child_be.models.Conversation;

@Repository
public interface ChatlogRepository extends JpaRepository<Chatlog, Long> {
    List<Chatlog> findBySender_Username(String username);
    Chatlog findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
    List<Chatlog> findByConversation(Conversation conversation);
}
