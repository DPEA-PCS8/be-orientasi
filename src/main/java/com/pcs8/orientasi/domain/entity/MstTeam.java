package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "mst_team")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MstTeam extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pic_uuid")
    private MstUser pic;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MstTeamMember> teamMembers = new HashSet<>();

    /**
     * Helper method to add a member to the team
     */
    public void addMember(MstUser user) {
        MstTeamMember teamMember = MstTeamMember.builder()
                .team(this)
                .user(user)
                .build();
        teamMembers.add(teamMember);
    }

    /**
     * Helper method to remove a member from the team
     */
    public void removeMember(MstUser user) {
        teamMembers.removeIf(tm -> tm.getUser().getUuid().equals(user.getUuid()));
    }

    /**
     * Helper method to clear all members
     */
    public void clearMembers() {
        teamMembers.clear();
    }
}
