package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mst_variable", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kategori", "kode"})
})
public class MstVariable extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kategori", nullable = false, length = 100)
    private String kategori; // e.g., KATEGORI_PENGHARGAAN, TIPE_SISTEM, etc.

    @Column(name = "kode", nullable = false, length = 100)
    private String kode;

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "urutan")
    private Integer urutan;

    @Column(name = "is_active", nullable = false, columnDefinition = "BIT DEFAULT 1")
    @Builder.Default
    private Boolean isActive = true;
}
