package com.team.child_be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.ChildBattery;

@Repository
public interface ChildBatteryRepository extends JpaRepository<ChildBattery, Long> {
    List<ChildBattery> findByChild_Username(String username);
}
