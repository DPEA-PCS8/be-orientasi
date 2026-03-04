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
@Table(name = "mst_bidang", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kode_bidang"})
})
public class MstBidang extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kode_bidang", nullable = false, length = 50)
    private String kodeBidang;

    @Column(name = "nama_bidang", nullable = false, length = 255)
    private String namaBidang;
}
