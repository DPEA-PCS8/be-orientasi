package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entity for tracking changes to PKSI documents
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_pksi_changelog")
public class PksiChangelog extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pksi_document_id", nullable = false)
    @ToString.Exclude
    private PksiDocument pksiDocument;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_label", nullable = false, length = 255)
    private String fieldLabel;

    @Column(name = "old_value", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    @ToString.Exclude
    private MstUser updatedBy;

    @Column(name = "updated_by_name", nullable = false, length = 255)
    private String updatedByName;
}
