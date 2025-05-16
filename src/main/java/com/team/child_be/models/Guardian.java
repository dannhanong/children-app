package com.team.child_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "guardian")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guardian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User primaryChild;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "guardian_child", 
        joinColumns = @JoinColumn(name = "guardian_id"),
        inverseJoinColumns = @JoinColumn(name = "child_id")
    )
    @Builder.Default
    private Set<User> additionalChildren = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    

}
