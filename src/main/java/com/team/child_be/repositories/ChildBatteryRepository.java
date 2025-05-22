package com.team.child_be.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.ChildBattery;
import com.team.child_be.models.User;

@Repository
public interface ChildBatteryRepository extends JpaRepository<ChildBattery, Long> {
    Optional<ChildBattery> findByChild(User child);
    void deleteByChild(User child);
}
