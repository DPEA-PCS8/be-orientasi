package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<MstProgram, UUID> {

    @Query("SELECT p FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId " +
           "AND p.yearVersion = :yearVersion " +
           "AND p.isActive = true " +
           "ORDER BY p.sequenceOrder ASC")
    Page<MstProgram> findByRbsiIdAndYearVersion(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion,
            Pageable pageable);

    @Query("SELECT p FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId " +
           "AND p.yearVersion = :yearVersion " +
           "AND p.isActive = true " +
           "ORDER BY p.sequenceOrder ASC")
    List<MstProgram> findAllByRbsiIdAndYearVersion(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion);

    @Query("SELECT p FROM MstProgram p " +
           "WHERE p.id = :id AND p.isActive = true")
    Optional<MstProgram> findByIdAndActive(@Param("id") UUID id);

    @Query("SELECT COALESCE(MAX(p.sequenceOrder), 0) FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId AND p.yearVersion = :yearVersion AND p.isActive = true")
    Integer findMaxSequenceOrder(@Param("rbsiId") UUID rbsiId, @Param("yearVersion") Integer yearVersion);

    @Query("SELECT p FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId " +
           "AND p.yearVersion = :yearVersion " +
           "AND p.sequenceOrder > :sequenceOrder " +
           "AND p.isActive = true " +
           "ORDER BY p.sequenceOrder ASC")
    List<MstProgram> findProgramsAfterSequence(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion,
            @Param("sequenceOrder") Integer sequenceOrder);

    @Query("SELECT COUNT(p) FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId AND p.isActive = true")
    Long countByRbsiId(@Param("rbsiId") UUID rbsiId);
}
