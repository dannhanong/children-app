package com.team.child_be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.ChatFolder;

@Repository
public interface ChatFolderRepository extends JpaRepository<ChatFolder, Long> {
    List<ChatFolder> findByUser_Username(String username);
    Optional<ChatFolder> findByIdAndUser_Username(Long id, String username);
    boolean existsByNameAndUser_Username(String name, String username);
}
