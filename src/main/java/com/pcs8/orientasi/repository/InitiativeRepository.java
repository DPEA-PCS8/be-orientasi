package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstInitiative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InitiativeRepository extends JpaRepository<MstInitiative, UUID> {

    @Query("SELECT i FROM MstInitiative i " +
           "WHERE i.program.id = :programId AND i.yearVersion = :yearVersion " +
           "ORDER BY i.sortOrder ASC")
    List<MstInitiative> findByProgramIdAndYearVersion(
            @Param("programId") UUID programId,
            @Param("yearVersion") Integer yearVersion);

    @Query("SELECT COALESCE(MAX(i.sortOrder), 0) FROM MstInitiative i " +
           "WHERE i.program.id = :programId AND i.yearVersion = :yearVersion")
    Integer findMaxSortOrder(@Param("programId") UUID programId, @Param("yearVersion") Integer yearVersion);

    @Modifying
    @Query("UPDATE MstInitiative i SET i.sortOrder = i.sortOrder + 1 " +
           "WHERE i.program.id = :programId AND i.yearVersion = :yearVersion AND i.sortOrder >= :fromSortOrder")
    void incrementSortOrderFrom(
            @Param("programId") UUID programId,
            @Param("yearVersion") Integer yearVersion,
            @Param("fromSortOrder") Integer fromSortOrder);

    @Query("SELECT i FROM MstInitiative i WHERE i.program.rbsi.id = :rbsiId AND i.yearVersion = :yearVersion")
    List<MstInitiative> findByRbsiIdAndYearVersion(
            @Param("rbsiId") UUID rbsiId,
            @Param("yearVersion") Integer yearVersion);
}
