package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Fs2Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Fs2DocumentRepository extends JpaRepository<Fs2Document, UUID> {

    List<Fs2Document> findAllByOrderByCreatedAtDesc();

    List<Fs2Document> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.namaFs2) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchFs2Documents(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.namaFs2) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "ORDER BY f.createdAt DESC")
    List<Fs2Document> searchFs2DocumentsList(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("status") String status
    );

    // Search only approved documents (for F.S.2 Disetujui page)
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.namaFs2) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2Documents(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("progres") String progres,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            Pageable pageable
    );

    // Overloaded method using filter object to comply with max 7 parameters rule
    default Page<Fs2Document> searchApprovedFs2Documents(
            com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter filter,
            Pageable pageable
    ) {
        return searchApprovedFs2Documents(
                filter.getSearch(),
                filter.getBidangId(),
                filter.getSkpaId(),
                filter.getProgres(),
                filter.getFasePengajuan(),
                filter.getMekanisme(),
                filter.getPelaksanaan(),
                pageable
        );
    }
}
