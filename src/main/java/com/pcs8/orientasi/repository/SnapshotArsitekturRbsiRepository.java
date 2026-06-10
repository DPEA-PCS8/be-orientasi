package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.SnapshotArsitekturRbsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SnapshotArsitekturRbsiRepository extends JpaRepository<SnapshotArsitekturRbsi, UUID> {

    List<SnapshotArsitekturRbsi> findByRbsiIdOrderBySnapshotDateDesc(UUID rbsiId);

    List<SnapshotArsitekturRbsi> findByRbsiIdAndSnapshotDateOrderByCreatedAtAsc(UUID rbsiId, LocalDate snapshotDate);

    Optional<SnapshotArsitekturRbsi> findByRbsiIdAndSnapshotDateAndArsitekturId(UUID rbsiId, LocalDate snapshotDate, UUID arsitekturId);

    List<LocalDate> findDistinctSnapshotDateByRbsiIdOrderBySnapshotDateDesc(UUID rbsiId);

    boolean existsByRbsiIdAndSnapshotDate(UUID rbsiId, LocalDate snapshotDate);

    @Query("SELECT s FROM SnapshotArsitekturRbsi s WHERE s.rbsi.id = :rbsiId AND s.arsitekturId = :arsitekturId AND s.snapshotDate < :date ORDER BY s.snapshotDate DESC LIMIT 1")
    Optional<SnapshotArsitekturRbsi> findLatestBeforeDate(@Param("rbsiId") UUID rbsiId, @Param("arsitekturId") UUID arsitekturId, @Param("date") LocalDate date);
}
