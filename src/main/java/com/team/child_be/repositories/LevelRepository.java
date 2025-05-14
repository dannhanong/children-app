package com.team.child_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.Level;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    Level findByChild_Id(Long childId);
    boolean existsByChild_Id(Long childId);
    void deleteByChild_Id(Long childId);
}
