package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiDocument;
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
public interface PksiDocumentRepository extends JpaRepository<PksiDocument, UUID> {
    
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.inisiatif ini LEFT JOIN FETCH ini.group LEFT JOIN FETCH p.inisiatifGroup WHERE p.user.uuid = :userUuid")
    List<PksiDocument> findByUserUuid(@Param("userUuid") UUID userUuid);
    
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.inisiatif ini LEFT JOIN FETCH ini.group LEFT JOIN FETCH p.inisiatifGroup WHERE p.user.uuid = :userUuid")
    List<PksiDocument> findByUserUuidWithAplikasi(@Param("userUuid") UUID userUuid);
    
    List<PksiDocument> findByStatus(PksiDocument.DocumentStatus status);
    
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.inisiatif ini LEFT JOIN FETCH ini.group LEFT JOIN FETCH p.inisiatifGroup")
    List<PksiDocument> findAllWithUser();
    
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.inisiatif ini LEFT JOIN FETCH ini.group LEFT JOIN FETCH p.inisiatifGroup WHERE p.id = :id")
    Optional<PksiDocument> findByIdWithUser(@Param("id") UUID id);

    @Query(value = "SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status)",
           countQuery = "SELECT COUNT(DISTINCT p) FROM PksiDocument p LEFT JOIN p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status)")
    Page<PksiDocument> searchDocuments(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status, 
            Pageable pageable);

    /**
     * Search PKSI documents with year filter based on timeline.
     * Year filter matches documents where the given year falls within
     * any of the timeline date ranges (tahap1 through tahap7).
     */
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND (:year IS NULL OR " +
           "(YEAR(p.tahap1Awal) = :year OR YEAR(p.tahap1Akhir) = :year OR " +
           "YEAR(p.tahap5Awal) = :year OR YEAR(p.tahap5Akhir) = :year OR " +
           "YEAR(p.tahap7Awal) = :year OR YEAR(p.tahap7Akhir) = :year))")
    Page<PksiDocument> searchDocumentsWithYear(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("year") Integer year,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM PksiDocument p WHERE p.status = :status")
    long countByStatus(@Param("status") PksiDocument.DocumentStatus status);

    /**
     * Search PKSI documents filtered by user department.
     * Matches documents where:
     * 1. aplikasi.skpa.kodeSkpa matches userDepartment, OR
     * 2. picSatker field contains SKPA ID whose kodeSkpa matches userDepartment
     * 
     * Note: userDepartment is validated at service layer to be non-null/non-empty.
     * CONCAT is used with internal database UUID values (skpa.id), not user input,
     * so this is safe from SQL injection.
     */
    @SuppressWarnings("java:S2077") // CONCAT uses internal UUID, not user input
    @Query(value = "SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u LEFT JOIN p.aplikasi a LEFT JOIN a.skpa s WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND ((s IS NOT NULL AND UPPER(s.kodeSkpa) = UPPER(:userDepartment)) OR " +
           "EXISTS (SELECT 1 FROM MstSkpa skpa WHERE UPPER(skpa.kodeSkpa) = UPPER(:userDepartment) AND p.picSatker LIKE CONCAT('%', CAST(skpa.id AS string), '%')))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM PksiDocument p LEFT JOIN p.user u LEFT JOIN p.aplikasi a LEFT JOIN a.skpa s WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND ((s IS NOT NULL AND UPPER(s.kodeSkpa) = UPPER(:userDepartment)) OR " +
           "EXISTS (SELECT 1 FROM MstSkpa skpa WHERE UPPER(skpa.kodeSkpa) = UPPER(:userDepartment) AND p.picSatker LIKE CONCAT('%', CAST(skpa.id AS string), '%')))")
    Page<PksiDocument> searchDocumentsByDepartment(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("userDepartment") String userDepartment,
            Pageable pageable);

    /**
     * Find PKSI documents by inisiatif group IDs
     */
    @Query("SELECT p FROM PksiDocument p WHERE p.inisiatifGroup.id IN :groupIds")
    List<PksiDocument> findByInisiatifGroupIdIn(@Param("groupIds") List<UUID> groupIds);

    /**
     * Find PKSI documents by inisiatif group IDs with status filter
     */
    @Query("SELECT p FROM PksiDocument p WHERE p.inisiatifGroup.id IN :groupIds " +
           "AND (:status IS NULL OR CAST(p.status AS string) = :status)")
    List<PksiDocument> findByInisiatifGroupIdInAndStatus(
            @Param("groupIds") List<UUID> groupIds, 
            @Param("status") String status);

    /**
     * Count PKSI documents by inisiatif group with status filter
     */
    @Query("SELECT p.inisiatifGroup.id, COUNT(p) FROM PksiDocument p " +
           "WHERE p.inisiatifGroup.id IN :groupIds " +
           "AND (:status IS NULL OR CAST(p.status AS string) = :status) " +
           "GROUP BY p.inisiatifGroup.id")
    List<Object[]> countByInisiatifGroupIdInAndStatus(
            @Param("groupIds") List<UUID> groupIds, 
            @Param("status") String status);

    /**
     * Search PKSI documents with year and noInisiatif filter.
     * - year: filter by year extracted from tanggal_pengajuan
     * - noInisiatif: if true, only return documents with null/empty program_inisiatif_rbsi
     */
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND (:year IS NULL OR YEAR(p.tanggalPengajuan) = :year) " +
           "AND (:noInisiatif = false OR p.programInisiatifRbsi IS NULL OR TRIM(p.programInisiatifRbsi) = '')")
    Page<PksiDocument> searchDocumentsWithFilters(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("noInisiatif") boolean noInisiatif,
            Pageable pageable);

    /**
     * Search PKSI documents filtered by user department with year and noInisiatif filter.
     */
    @SuppressWarnings("java:S2077") // CONCAT uses internal UUID, not user input
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u LEFT JOIN p.aplikasi a LEFT JOIN a.skpa s WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND (:year IS NULL OR YEAR(p.tanggalPengajuan) = :year) " +
           "AND (:noInisiatif = false OR p.programInisiatifRbsi IS NULL OR TRIM(p.programInisiatifRbsi) = '') " +
           "AND ((s IS NOT NULL AND UPPER(s.kodeSkpa) = UPPER(:userDepartment)) OR " +
           "EXISTS (SELECT 1 FROM MstSkpa skpa WHERE UPPER(skpa.kodeSkpa) = UPPER(:userDepartment) AND p.picSatker LIKE CONCAT('%', CAST(skpa.id AS string), '%')))")
    Page<PksiDocument> searchDocumentsByDepartmentWithFilters(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("noInisiatif") boolean noInisiatif,
            @Param("userDepartment") String userDepartment,
            Pageable pageable);

    /**
     * Count PKSI documents with optional year and noInisiatif filters.
     * Used for displaying total count in monitoring page.
     */
    @Query("SELECT COUNT(p) FROM PksiDocument p WHERE " +
           "(:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND (:year IS NULL OR YEAR(p.tanggalPengajuan) = :year) " +
           "AND (:noInisiatif = false OR p.programInisiatifRbsi IS NULL OR TRIM(p.programInisiatifRbsi) = '')")
    long countByStatusYearAndNoInisiatif(
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("noInisiatif") boolean noInisiatif);
}
