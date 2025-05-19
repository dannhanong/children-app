package com.team.child_be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.team.child_be.models.Call;
import com.team.child_be.models.User;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
    List<Call> findByCaller(User caller);
    
    List<Call> findByReceiver(User receiver);
    
    @Query("SELECT c FROM Call c WHERE c.caller.id = :userId OR c.receiver.id = :userId ORDER BY c.startTime DESC")
    List<Call> findCallHistoryByUserId(Long userId);
    
    Optional<Call> findByChannelName(String channelName);
}
