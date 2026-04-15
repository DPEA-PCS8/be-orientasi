package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.AplikasiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AplikasiSnapshotRepository extends JpaRepository<AplikasiSnapshot, UUID> {

    Optional<AplikasiSnapshot> findByAplikasiIdAndTahun(UUID aplikasiId, Integer tahun);

    List<AplikasiSnapshot> findByTahunOrderByNamaAplikasi(Integer tahun);

    List<AplikasiSnapshot> findByTahunBetweenOrderByTahunDescNamaAplikasiAsc(Integer startYear, Integer endYear);

    @Query("SELECT DISTINCT s.tahun FROM AplikasiSnapshot s ORDER BY s.tahun DESC")
    List<Integer> findDistinctTahun();

    @Query("SELECT s FROM AplikasiSnapshot s WHERE s.tahun BETWEEN :startYear AND :endYear ORDER BY s.namaAplikasi, s.tahun")
    List<AplikasiSnapshot> findByPeriode(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    @Query("SELECT COUNT(s) FROM AplikasiSnapshot s WHERE s.tahun = :tahun")
    Long countByTahun(@Param("tahun") Integer tahun);

    @Query("SELECT s.statusAplikasi, COUNT(s) FROM AplikasiSnapshot s WHERE s.tahun = :tahun GROUP BY s.statusAplikasi")
    List<Object[]> countByStatusAndTahun(@Param("tahun") Integer tahun);

    @Query("SELECT COUNT(s) FROM AplikasiSnapshot s WHERE s.tahun = :tahun AND s.statusAplikasi = 'AKTIF'")
    Long countAktifByTahun(@Param("tahun") Integer tahun);

    boolean existsByAplikasiIdAndTahun(UUID aplikasiId, Integer tahun);

    List<AplikasiSnapshot> findByAplikasiIdOrderByTahunDesc(UUID aplikasiId);

    void deleteByAplikasiIdAndTahun(UUID aplikasiId, Integer tahun);
}
