package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_changelog")
public class AplikasiChangelog extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    @ToString.Exclude
    private AplikasiSnapshot snapshot;

    @Column(name = "tanggal_perubahan", nullable = false)
    private LocalDate tanggalPerubahan;

    @Column(name = "keterangan", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String keterangan;

    @Column(name = "perubahan_detail", columnDefinition = "NVARCHAR(MAX)")
    private String perubahanDetail; // JSON format for detailed changes
}
