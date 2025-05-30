package com.team.child_be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.DeviceToken;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUser_IdAndActiveTrue(Long userId);
    Optional<DeviceToken> findByTokenContaining(String token);
    Optional<DeviceToken> findByTokenContainingAndUser_Username(String token, String username);
    boolean existsByToken(String token);
    void deleteByToken(String token);
    boolean existsByUser_UsernameAndToken(String username, String token);
    void deleteByUser_Username(String username);
    DeviceToken findFirstByUser_IdAndActiveTrueOrderByIdDesc(Long userId);
}
