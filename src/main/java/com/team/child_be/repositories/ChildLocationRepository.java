package com.team.child_be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.ChildLocation;
import com.team.child_be.models.User;

@Repository
public interface ChildLocationRepository extends JpaRepository<ChildLocation, Long> {
    List<ChildLocation> findByUser_Username(String username);
    List<ChildLocation> findByUser(User user);
}
