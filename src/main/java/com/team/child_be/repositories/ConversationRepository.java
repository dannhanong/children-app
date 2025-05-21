package com.team.child_be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserOne_UsernameOrUserTwo_Username(String usernameOne, String usernameTwo);
    Conversation findByUserOne_UsernameAndUserTwo_UsernameOrUserTwo_UsernameAndUserOne_Username(String usernameOne, String usernameTwo, String usernameTwo2, String usernameOne2);
}
