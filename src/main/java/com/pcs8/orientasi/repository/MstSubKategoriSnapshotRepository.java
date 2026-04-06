package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstSubKategoriSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MstSubKategoriSnapshotRepository extends JpaRepository<MstSubKategoriSnapshot, UUID> {

    List<MstSubKategoriSnapshot> findBySnapshotYearOrderByKodeAsc(Integer snapshotYear);

    List<MstSubKategoriSnapshot> findBySubKategoriIdOrderBySnapshotYearDescSnapshotDateDesc(UUID subKategoriId);

    @Query("SELECT DISTINCT s.snapshotYear FROM MstSubKategoriSnapshot s ORDER BY s.snapshotYear DESC")
    List<Integer> findDistinctSnapshotYears();

    @Query("SELECT s FROM MstSubKategoriSnapshot s WHERE s.snapshotYear = :year ORDER BY s.categoryCode ASC, s.kode ASC")
    List<MstSubKategoriSnapshot> findByYearOrderByCategoryAndKode(@Param("year") Integer year);

    boolean existsBySnapshotYearAndSubKategoriId(Integer snapshotYear, UUID subKategoriId);

    @Query("SELECT COUNT(DISTINCT s.subKategori.id) FROM MstSubKategoriSnapshot s WHERE s.snapshotYear = :year")
    Long countDistinctSubKategoriByYear(@Param("year") Integer year);
}
