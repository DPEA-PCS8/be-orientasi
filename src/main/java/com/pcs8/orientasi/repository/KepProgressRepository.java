package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.KepProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KepProgressRepository extends JpaRepository<KepProgress, UUID> {

    List<KepProgress> findByKepIdAndInisiatifGroupIdOrderByTahunAsc(UUID kepId, UUID inisiatifGroupId);

    List<KepProgress> findByKepIdOrderByInisiatifGroupIdAscTahunAsc(UUID kepId);

    Optional<KepProgress> findByKepIdAndInisiatifGroupIdAndTahun(UUID kepId, UUID inisiatifGroupId, Integer tahun);

    boolean existsByKepIdAndInisiatifGroupIdAndTahun(UUID kepId, UUID inisiatifGroupId, Integer tahun);

    @Query("SELECT kp FROM KepProgress kp WHERE kp.kep.rbsi.id = :rbsiId ORDER BY kp.inisiatifGroup.id, kp.kep.tahunPelaporan, kp.tahun")
    List<KepProgress> findAllByRbsiId(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT kp FROM KepProgress kp WHERE kp.kep.rbsi.id = :rbsiId AND kp.kep.id = :kepId ORDER BY kp.inisiatifGroup.id, kp.tahun")
    List<KepProgress> findByRbsiIdAndKepId(@Param("rbsiId") UUID rbsiId, @Param("kepId") UUID kepId);

    void deleteByKepId(UUID kepId);

    // Analytics queries - optimized for performance
    @Query("SELECT kp FROM KepProgress kp WHERE kp.kep.id IN :kepIds ORDER BY kp.kep.id, kp.inisiatifGroup.id, kp.tahun")
    List<KepProgress> findByKepIdIn(@Param("kepIds") List<UUID> kepIds);

    @Query("SELECT kp.kep.id, kp.tahun, COUNT(kp) FROM KepProgress kp " +
           "WHERE kp.kep.id IN :kepIds AND kp.status != 'none' " +
           "GROUP BY kp.kep.id, kp.tahun")
    List<Object[]> countRealizedByKepIdAndYear(@Param("kepIds") List<UUID> kepIds);
}
