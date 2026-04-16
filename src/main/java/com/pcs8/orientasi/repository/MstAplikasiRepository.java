package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstAplikasi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstAplikasiRepository extends JpaRepository<MstAplikasi, UUID> {

    Optional<MstAplikasi> findByKodeAplikasi(String kodeAplikasi);

    boolean existsByKodeAplikasi(String kodeAplikasi);

    List<MstAplikasi> findAllByOrderByKodeAplikasiAsc();

    @Query("SELECT a FROM MstAplikasi a ORDER BY a.kodeAplikasi ASC")
    Page<MstAplikasi> findAllLightweight(Pageable pageable);

    @Query("SELECT a FROM MstAplikasi a ORDER BY a.kodeAplikasi ASC")
    List<MstAplikasi> findAllLightweightList();

    @Query("SELECT a FROM MstAplikasi a WHERE " +
           "(:search IS NULL OR LOWER(a.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.kodeAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR a.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR a.skpa.id = :skpaId) " +
           "AND (:status IS NULL OR a.statusAplikasi = :status) " +
           "ORDER BY a.namaAplikasi ASC")
    Page<MstAplikasi> searchAplikasi(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT a FROM MstAplikasi a WHERE " +
           "(:search IS NULL OR LOWER(a.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.kodeAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR a.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR a.skpa.id = :skpaId) " +
           "AND (:status IS NULL OR a.statusAplikasi = :status) " +
           "ORDER BY a.namaAplikasi ASC")
    List<MstAplikasi> searchAplikasiList(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("status") String status
    );
}
