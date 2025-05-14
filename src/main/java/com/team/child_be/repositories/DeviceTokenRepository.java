package com.team.child_be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.DeviceToken;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUser_IdAndActiveTrue(Long userId);
    Optional<DeviceToken> findByToken(String token);
    boolean existsByToken(String token);
    void deleteByToken(String token);
}
