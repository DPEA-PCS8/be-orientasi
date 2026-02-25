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

    List<KepProgress> findByKepIdAndInisiatifIdOrderByTahunAsc(UUID kepId, UUID inisiatifId);

    List<KepProgress> findByKepIdOrderByInisiatifIdAscTahunAsc(UUID kepId);

    Optional<KepProgress> findByKepIdAndInisiatifIdAndTahun(UUID kepId, UUID inisiatifId, Integer tahun);

    boolean existsByKepIdAndInisiatifIdAndTahun(UUID kepId, UUID inisiatifId, Integer tahun);

    @Query("SELECT kp FROM KepProgress kp WHERE kp.kep.rbsi.id = :rbsiId ORDER BY kp.inisiatif.id, kp.kep.tahunPelaporan, kp.tahun")
    List<KepProgress> findAllByRbsiId(@Param("rbsiId") UUID rbsiId);

    void deleteByKepId(UUID kepId);
}
