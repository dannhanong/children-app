package com.team.child_be.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Mission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "Phụ huynh không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    User parent;

    @NotNull(message = "Trẻ em không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "child_id", referencedColumnName = "id")
    User child;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    String title;

    @NotBlank(message = "Mô tả không được để trống")
    String description;

    @NotNull(message = "Hạn chót không được để trống")
    LocalDateTime deadline;
    
    Double points;

    LocalDateTime completedAt;
    boolean confirm;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
    LocalDateTime deletedAt;
}
