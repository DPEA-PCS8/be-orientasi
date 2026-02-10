package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mst_initiative", indexes = {
    @Index(name = "idx_initiative_program_year", columnList = "program_id, year_version"),
    @Index(name = "idx_initiative_sort_order", columnList = "program_id, year_version, sort_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MstInitiative {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private MstProgram program;

    @Column(name = "initiative_number", nullable = false, length = 30)
    private String initiativeNumber;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "year_version", nullable = false)
    private Integer yearVersion;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "pending";

    @Column(name = "link_dokumen", length = 500)
    private String linkDokumen;

    @Column(name = "tanggal_submit")
    private LocalDateTime tanggalSubmit;

    @Column(name = "pksi_relation_id")
    private UUID pksiRelationId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
