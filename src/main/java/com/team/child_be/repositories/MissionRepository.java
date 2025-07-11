package com.team.child_be.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    Page<Mission> findByParent_IdAndDeletedAtNull(Long parentId, Pageable pageable);
    Page<Mission> findByChild_IdAndDeletedAtNull(Long childId, Pageable pageable);
    Optional<Mission> findByIdAndChild_UsernameAndDeletedAtNull(Long id, String username);
    Optional<Mission> findByIdAndParent_UsernameAndDeletedAtNull(Long id, String username);

    @Query("SELECT m FROM Mission m WHERE m.title LIKE %:title% AND (m.parent.username = :parentUsername OR m.child.username = :childUsername) AND m.deletedAt IS NULL")
    Page<Mission> findByTitleContainingAndParent_UsernameOrChild_UsernameAndDeletedAtIsNull(String title, String parentUsername, String childUsername, Pageable pageable);
}
