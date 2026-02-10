package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstInitiative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InitiativeRepository extends JpaRepository<MstInitiative, UUID> {

    @Query("SELECT i FROM MstInitiative i " +
           "WHERE i.program.id = :programId " +
           "AND i.yearVersion = :yearVersion " +
           "AND i.isActive = true " +
           "ORDER BY i.sequenceOrder ASC")
    List<MstInitiative> findByProgramIdAndYearVersion(
            @Param("programId") UUID programId,
            @Param("yearVersion") Integer yearVersion);

    @Query("SELECT i FROM MstInitiative i " +
           "WHERE i.id = :id AND i.isActive = true")
    Optional<MstInitiative> findByIdAndActive(@Param("id") UUID id);

    @Query("SELECT COALESCE(MAX(i.sequenceOrder), 0) FROM MstInitiative i " +
           "WHERE i.program.id = :programId AND i.yearVersion = :yearVersion AND i.isActive = true")
    Integer findMaxSequenceOrder(@Param("programId") UUID programId, @Param("yearVersion") Integer yearVersion);

    @Query("SELECT COUNT(i) FROM MstInitiative i " +
           "WHERE i.program.id = :programId AND i.isActive = true")
    Long countByProgramId(@Param("programId") UUID programId);
}
