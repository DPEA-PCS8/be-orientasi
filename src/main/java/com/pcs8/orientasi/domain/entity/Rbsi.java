package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mst_rbsi", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"periode"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Rbsi extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "periode", nullable = false, length = 20)
    private String periode;

    @OneToMany(mappedBy = "rbsi", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<RbsiProgram> programs = new ArrayList<>();
}
