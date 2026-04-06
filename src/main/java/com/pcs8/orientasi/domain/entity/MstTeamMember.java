package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mst_team_member", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "user_uuid"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MstTeamMember {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private MstTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false)
    private MstUser user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MstTeamMember that = (MstTeamMember) o;
        // Use team and user for equality when id is null (new entity)
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return team != null && user != null 
            && team.getId() != null && that.team != null && that.team.getId() != null
            && user.getUuid() != null && that.user != null && that.user.getUuid() != null
            && team.getId().equals(that.team.getId()) 
            && user.getUuid().equals(that.user.getUuid());
    }

    @Override
    public int hashCode() {
        // Use a constant hashCode to ensure new entities can be added to HashSet
        // This is a common pattern for JPA entities with generated IDs
        return getClass().hashCode();
    }
}
