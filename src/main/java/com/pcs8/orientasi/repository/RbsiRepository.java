package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstRbsi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RbsiRepository extends JpaRepository<MstRbsi, UUID> {

    @Query("SELECT r FROM MstRbsi r WHERE r.isActive = true ORDER BY r.createdAt DESC")
    Page<MstRbsi> findAllActive(Pageable pageable);

    @Query("SELECT r FROM MstRbsi r WHERE r.periode = :periode AND r.isActive = true")
    Optional<MstRbsi> findByPeriodeAndActive(@Param("periode") String periode);

    @Query("SELECT r FROM MstRbsi r WHERE r.id = :id AND r.isActive = true")
    Optional<MstRbsi> findByIdAndActive(@Param("id") UUID id);

    @Query("SELECT COUNT(r) > 0 FROM MstRbsi r WHERE r.periode = :periode AND r.isActive = true")
    boolean existsByPeriode(@Param("periode") String periode);
}
