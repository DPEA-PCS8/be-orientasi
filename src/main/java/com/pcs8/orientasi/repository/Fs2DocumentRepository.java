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
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
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

    /**
     * Search F.S.2 documents with year filter based on tanggal_pengajuan.
     */
    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchFs2DocumentsWithYear(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("status") String status,
            @Param("year") Integer year,
            Pageable pageable
    );

    /**
     * Search F.S.2 documents with year and month range filter based on tanggal_pengajuan.
     */
    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:aplikasiId IS NULL OR f.aplikasi.id = :aplikasiId) " +
           "AND (:statusTahapan IS NULL OR f.statusTahapan = :statusTahapan) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "AND (:startMonth IS NULL OR MONTH(f.tanggalPengajuan) >= :startMonth) " +
           "AND (:endMonth IS NULL OR MONTH(f.tanggalPengajuan) <= :endMonth) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchFs2DocumentsWithYearAndMonth(
            @Param("search") String search,
            @Param("aplikasiId") UUID aplikasiId,
            @Param("statusTahapan") String statusTahapan,
            @Param("skpaId") UUID skpaId,
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth,
            Pageable pageable
    );

    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
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
    @SuppressWarnings("java:S107") // Parameters needed for JPQL query filtering
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:progresStatus IS NULL OR f.progresStatus = :progresStatus) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2Documents(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("progres") String progres,
            @Param("progresStatus") String progresStatus,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            Pageable pageable
    );

    /**
     * Search only approved documents with year filter.
     * Year filter matches by YEAR(tanggalPengajuan) to be consistent with PKSI.
     */
    @SuppressWarnings("java:S107") // Parameters needed for JPQL query filtering
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:progresStatus IS NULL OR f.progresStatus = :progresStatus) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2DocumentsWithYear(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("progres") String progres,
            @Param("progresStatus") String progresStatus,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            @Param("year") Integer year,
            Pageable pageable
    );

    /**
     * Search only approved documents with year and month range filter.
     */
    @SuppressWarnings("java:S107") // Parameters needed for JPQL query filtering
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (:skpaId IS NULL OR f.skpa.id = :skpaId) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:progresStatus IS NULL OR f.progresStatus = :progresStatus) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "AND (:startMonth IS NULL OR MONTH(f.tanggalPengajuan) >= :startMonth) " +
           "AND (:endMonth IS NULL OR MONTH(f.tanggalPengajuan) <= :endMonth) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2DocumentsWithYearAndMonth(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("skpaId") UUID skpaId,
            @Param("progres") String progres,
            @Param("progresStatus") String progresStatus,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth,
            Pageable pageable
    );

    // Overloaded method using filter object to comply with max 7 parameters rule
    default Page<Fs2Document> searchApprovedFs2Documents(
            com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter filter,
            Pageable pageable
    ) {
        if (filter.getYear() != null || filter.getStartMonth() != null || filter.getEndMonth() != null) {
            return searchApprovedFs2DocumentsWithYearAndMonth(
                    filter.getSearch(),
                    filter.getBidangId(),
                    filter.getSkpaId(),
                    filter.getProgres(),
                    filter.getProgresStatus(),
                    filter.getFasePengajuan(),
                    filter.getMekanisme(),
                    filter.getPelaksanaan(),
                    filter.getYear(),
                    filter.getStartMonth(),
                    filter.getEndMonth(),
                    pageable
            );
        }
        return searchApprovedFs2Documents(
                filter.getSearch(),
                filter.getBidangId(),
                filter.getSkpaId(),
                filter.getProgres(),
                filter.getProgresStatus(),
                filter.getFasePengajuan(),
                filter.getMekanisme(),
                filter.getPelaksanaan(),
                pageable
        );
    }

    // Search F.S.2 documents filtered by SKPA department (kode_skpa)
    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchFs2DocumentsByDepartment(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("status") String status,
            @Param("userDepartment") String userDepartment,
            Pageable pageable
    );

    /**
     * Search F.S.2 documents filtered by SKPA department with year filter.
     */
    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchFs2DocumentsByDepartmentWithYear(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("status") String status,
            @Param("userDepartment") String userDepartment,
            @Param("year") Integer year,
            Pageable pageable
    );

    /**
     * Search F.S.2 documents filtered by SKPA department with year and month range filter.
     */
    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:aplikasiId IS NULL OR f.aplikasi.id = :aplikasiId) " +
           "AND (:statusTahapan IS NULL OR f.statusTahapan = :statusTahapan) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "AND (:startMonth IS NULL OR MONTH(f.tanggalPengajuan) >= :startMonth) " +
           "AND (:endMonth IS NULL OR MONTH(f.tanggalPengajuan) <= :endMonth) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchFs2DocumentsByDepartmentWithYearAndMonth(
            @Param("search") String search,
            @Param("aplikasiId") UUID aplikasiId,
            @Param("statusTahapan") String statusTahapan,
            @Param("status") String status,
            @Param("userDepartment") String userDepartment,
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth,
            Pageable pageable
    );

    // Search F.S.2 documents list filtered by SKPA department (kode_skpa)
    @Query("SELECT f FROM Fs2Document f WHERE " +
           "(:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:status IS NULL OR f.status = :status) " +
           "ORDER BY f.createdAt DESC")
    List<Fs2Document> searchFs2DocumentsListByDepartment(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("status") String status,
            @Param("userDepartment") String userDepartment
    );

    // Search approved F.S.2 documents filtered by SKPA department
    @SuppressWarnings("java:S107") // Parameters needed for JPQL query filtering
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:progresStatus IS NULL OR f.progresStatus = :progresStatus) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2DocumentsByDepartment(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("progres") String progres,
            @Param("progresStatus") String progresStatus,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            @Param("userDepartment") String userDepartment,
            Pageable pageable
    );

    /**
     * Search approved F.S.2 documents filtered by SKPA department with year filter.
     * Year filter matches by YEAR(tanggalPengajuan) to be consistent with PKSI.
     */
    @SuppressWarnings("java:S107") // Parameters needed for JPQL query filtering
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:progresStatus IS NULL OR f.progresStatus = :progresStatus) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2DocumentsByDepartmentWithYear(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("progres") String progres,
            @Param("progresStatus") String progresStatus,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            @Param("userDepartment") String userDepartment,
            @Param("year") Integer year,
            Pageable pageable
    );

    /**
     * Search approved F.S.2 documents filtered by SKPA department with year and month range filter.
     */
    @SuppressWarnings("java:S107") // Parameters needed for JPQL query filtering
    @Query("SELECT f FROM Fs2Document f WHERE f.status = 'DISETUJUI' " +
           "AND (:search IS NULL OR LOWER(f.aplikasi.namaAplikasi) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:bidangId IS NULL OR f.bidang.id = :bidangId) " +
           "AND (f.skpa IS NOT NULL AND UPPER(f.skpa.kodeSkpa) = UPPER(:userDepartment)) " +
           "AND (:progres IS NULL OR f.progres = :progres) " +
           "AND (:progresStatus IS NULL OR f.progresStatus = :progresStatus) " +
           "AND (:fasePengajuan IS NULL OR f.fasePengajuan = :fasePengajuan) " +
           "AND (:mekanisme IS NULL OR f.mekanisme = :mekanisme) " +
           "AND (:pelaksanaan IS NULL OR f.pelaksanaan = :pelaksanaan) " +
           "AND (:year IS NULL OR YEAR(f.tanggalPengajuan) = :year) " +
           "AND (:startMonth IS NULL OR MONTH(f.tanggalPengajuan) >= :startMonth) " +
           "AND (:endMonth IS NULL OR MONTH(f.tanggalPengajuan) <= :endMonth) " +
           "ORDER BY f.createdAt DESC")
    Page<Fs2Document> searchApprovedFs2DocumentsByDepartmentWithYearAndMonth(
            @Param("search") String search,
            @Param("bidangId") UUID bidangId,
            @Param("progres") String progres,
            @Param("progresStatus") String progresStatus,
            @Param("fasePengajuan") String fasePengajuan,
            @Param("mekanisme") String mekanisme,
            @Param("pelaksanaan") String pelaksanaan,
            @Param("userDepartment") String userDepartment,
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth,
            Pageable pageable
    );

    // Overloaded method using filter object for approved documents by department
    default Page<Fs2Document> searchApprovedFs2DocumentsByDepartment(
            com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter filter,
            String userDepartment,
            Pageable pageable
    ) {
        if (filter.getYear() != null || filter.getStartMonth() != null || filter.getEndMonth() != null) {
            return searchApprovedFs2DocumentsByDepartmentWithYearAndMonth(
                    filter.getSearch(),
                    filter.getBidangId(),
                    filter.getProgres(),
                    filter.getProgresStatus(),
                    filter.getFasePengajuan(),
                    filter.getMekanisme(),
                    filter.getPelaksanaan(),
                    userDepartment,
                    filter.getYear(),
                    filter.getStartMonth(),
                    filter.getEndMonth(),
                    pageable
            );
        }
        return searchApprovedFs2DocumentsByDepartment(
                filter.getSearch(),
                filter.getBidangId(),
                filter.getProgres(),
                filter.getProgresStatus(),
                filter.getFasePengajuan(),
                filter.getMekanisme(),
                filter.getPelaksanaan(),
                userDepartment,
                pageable
        );
    }
}
