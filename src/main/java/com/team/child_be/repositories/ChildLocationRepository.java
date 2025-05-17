package com.team.child_be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.ChildLocation;
import com.team.child_be.models.User;

@Repository
public interface ChildLocationRepository extends JpaRepository<ChildLocation, Long> {
    Page<ChildLocation> findByChild(User child, Pageable pageable);    
}
