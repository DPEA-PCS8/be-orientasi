package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "mst_user")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MstUser extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "uuid", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MstUserRole> userRoles = new HashSet<>();

    // Helper method to check if user has any role
    public boolean hasRole() {
        return userRoles != null && !userRoles.isEmpty();
    }

    // Helper method to get role names
    public Set<String> getRoleNames() {
        if (userRoles == null) return new HashSet<>();
        return userRoles.stream()
                .map(ur -> ur.getRole().getRoleName())
                .collect(Collectors.toSet());
    }
}