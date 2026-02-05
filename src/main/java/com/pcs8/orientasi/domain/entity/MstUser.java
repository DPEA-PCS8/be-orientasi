package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mst_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MstUser {

    @Id
    @UuidGenerator
    @Column(name = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "department", length = 255)
    private String department;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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