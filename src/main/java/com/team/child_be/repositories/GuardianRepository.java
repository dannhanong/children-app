package com.team.child_be.repositories;

import com.team.child_be.models.Guardian;
import com.team.child_be.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    List<Guardian> findByCreatedById(Long createdById);

    @Query("SELECT g FROM Guardian g WHERE g.primaryChild.id = :childId OR :childId IN (SELECT c.id FROM g.additionalChildren c)")
    List<Guardian> findByAnyChildId(@Param("childId") Long childId);
    

}
