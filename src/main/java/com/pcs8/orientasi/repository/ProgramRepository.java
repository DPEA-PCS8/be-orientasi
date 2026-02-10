package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstProgram;
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
           "LEFT JOIN FETCH p.initiatives i " +
           "WHERE p.rbsi.id = :rbsiId AND p.yearVersion = :yearVersion " +
           "ORDER BY p.sortOrder ASC, i.sortOrder ASC")
    List<MstProgram> findByRbsiIdAndYearVersionWithInitiatives(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion);

    @Query("SELECT p FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId AND p.yearVersion = :yearVersion " +
           "ORDER BY p.sortOrder ASC")
    List<MstProgram> findByRbsiIdAndYearVersion(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion);

    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM MstProgram p " +
           "WHERE p.rbsi.id = :rbsiId AND p.yearVersion = :yearVersion")
    Integer findMaxSortOrder(@Param("rbsiId") UUID rbsiId, @Param("yearVersion") Integer yearVersion);

    @Modifying
    @Query("UPDATE MstProgram p SET p.sortOrder = p.sortOrder + 1, p.programNumber = CONCAT('3.', p.sortOrder + 1) " +
           "WHERE p.rbsi.id = :rbsiId AND p.yearVersion = :yearVersion AND p.sortOrder >= :fromSortOrder")
    void incrementSortOrderFrom(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion,
            @Param("fromSortOrder") Integer fromSortOrder);

    @Query("SELECT DISTINCT p.yearVersion FROM MstProgram p WHERE p.rbsi.id = :rbsiId ORDER BY p.yearVersion DESC")
    List<Integer> findDistinctYearVersionsByRbsiId(@Param("rbsiId") UUID rbsiId);

    Optional<MstProgram> findByIdAndYearVersion(UUID id, Integer yearVersion);
}
