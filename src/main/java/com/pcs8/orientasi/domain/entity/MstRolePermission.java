package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "mst_role_permission", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_id", "menu_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MstRolePermission extends BaseEntity {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private MstRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MstMenu menu;

    @Column(nullable = false)
    @Builder.Default
    private Boolean canView = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean canCreate = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean canUpdate = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean canDelete = false;
}