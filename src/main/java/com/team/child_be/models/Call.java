package com.team.child_be.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "calls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne
    @JoinColumn(name = "caller_id")
    User caller;
    
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    User receiver;
    
    String channelName;
    
    LocalDateTime startTime;
    
    LocalDateTime endTime;
    
    Integer durationInSeconds;
    
    Boolean answered;
    
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
}