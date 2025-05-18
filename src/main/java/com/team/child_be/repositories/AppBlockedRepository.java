package com.team.child_be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.dtos.enums.AppType;
import com.team.child_be.models.AppBlocked;

@Repository
public interface AppBlockedRepository extends JpaRepository<AppBlocked, Long> {
    List<AppBlocked> findByChild_IdAndAppType(Long childId, AppType appType);
    List<AppBlocked> findByChild_ParentIdAndAppType(Long parentId, AppType appType);
    List<AppBlocked> findByChild_UsernameAndAppType(String username, AppType appType);
}
