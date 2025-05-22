package com.team.child_be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.ChatFolder;
import com.team.child_be.models.Chatlog;
import com.team.child_be.models.Conversation;
import com.team.child_be.models.User;

@Repository
public interface ChatlogRepository extends JpaRepository<Chatlog, Long> {
    List<Chatlog> findBySender_Username(String username);
    Chatlog findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
    List<Chatlog> findByConversation(Conversation conversation);

    @Query("SELECT c FROM Chatlog c JOIN c.folders f WHERE f.id = :folderId AND (c.sender = :user OR c.receiver = :user) AND c.message LIKE %:keyword%")
    List<Chatlog> findByFolderAndUser(@Param("folderId") Long folderId, @Param("user") User user, @Param("keyword") String keyword);
    boolean existsByIdAndFoldersContaining(Long chatlogId, ChatFolder folder);
    Optional<Chatlog> findByIdAndSenderOrIdAndReceiver(Long id, User sender, Long id2, User receiver);
}
