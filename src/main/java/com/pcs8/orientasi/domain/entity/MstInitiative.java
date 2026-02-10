package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mst_initiative")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MstInitiative {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private MstProgram program;

    @Column(name = "initiative_number", nullable = false, length = 30)
    private String initiativeNumber;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "year_version", nullable = false)
    private Integer yearVersion;

    @Column(name = "submit_date")
    private LocalDateTime submitDate;

    @Column(name = "document_link", length = 500)
    private String documentLink;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "pending";

    @Column(name = "pksi_relation_id")
    private UUID pksiRelationId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
