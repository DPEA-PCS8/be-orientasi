package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.RbsiInisiatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RbsiInisiatifRepository extends JpaRepository<RbsiInisiatif, UUID> {

    List<RbsiInisiatif> findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(UUID programId, Integer tahun);

    Optional<RbsiInisiatif> findByProgramIdAndTahunAndNomorInisiatifAndIsDeletedFalse(UUID programId, Integer tahun, String nomorInisiatif);

    boolean existsByProgramIdAndTahunAndNomorInisiatifAndIsDeletedFalse(UUID programId, Integer tahun, String nomorInisiatif);

    // For soft delete cascade - get all inisiatifs (including already deleted) by program
    List<RbsiInisiatif> findByProgramIdAndIsDeletedFalse(UUID programId);

    // Get all inisiatifs for multiple programs
    List<RbsiInisiatif> findByProgramIdInAndIsDeletedFalse(List<UUID> programIds);

    // Find inisiatif by group ID (latest version)
    @Query("SELECT i FROM RbsiInisiatif i WHERE i.group.id IN :groupIds AND i.isDeleted = false")
    List<RbsiInisiatif> findByGroupIdIn(@Param("groupIds") List<UUID> groupIds);

    @Query("SELECT i.group.id, i.nomorInisiatif FROM RbsiInisiatif i WHERE i.group.id IN :groupIds AND i.isDeleted = false AND i.tahun = (SELECT MAX(i2.tahun) FROM RbsiInisiatif i2 WHERE i2.group.id = i.group.id AND i2.isDeleted = false)")
    List<Object[]> findLatestNomorInisiatifByGroupId(@Param("groupIds") List<UUID> groupIds);
}
