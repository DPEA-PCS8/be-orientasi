package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.RbsiKep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RbsiKepRepository extends JpaRepository<RbsiKep, UUID> {

    List<RbsiKep> findByRbsiIdOrderByTahunPelaporanAsc(UUID rbsiId);

    Optional<RbsiKep> findByRbsiIdAndNomorKep(UUID rbsiId, String nomorKep);

    Optional<RbsiKep> findByRbsiIdAndTahunPelaporan(UUID rbsiId, Integer tahunPelaporan);

    boolean existsByRbsiIdAndNomorKep(UUID rbsiId, String nomorKep);

    boolean existsByRbsiIdAndTahunPelaporan(UUID rbsiId, Integer tahunPelaporan);

    Optional<RbsiKep> findTopByRbsiIdOrderByTahunPelaporanDesc(UUID rbsiId);
}
