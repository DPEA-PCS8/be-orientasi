package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.dto.response.ParentPksiSummary;
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
     * Search PKSI documents with year filter based on target timeline fields.
     */
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND (:year IS NULL OR " +
           "(YEAR(p.targetUsreq) = :year OR YEAR(p.targetSit) = :year OR " +
           "YEAR(p.targetUat) = :year OR YEAR(p.targetGoLive) = :year))")
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
     * Search PKSI documents with year, noInisiatif, and timeline filter.
     * - year: filter by year extracted from trn_pksi_timeline table
     * - noInisiatif: if true, only return documents with null/empty program_inisiatif_rbsi
     * - timelineStage: filter by timeline stage (USREQ, SIT, UAT, etc.)
     * - timelineFromMonth: filter by month range start (1-12)
     * - timelineToMonth: filter by month range end (1-12)
     * - timelineYear: filter by specific year for timeline date
     */
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status " +
           "OR (:status = 'DISETUJUI' AND CAST(p.status AS string) = 'DIKERJAKAN_DENGAN_CARA_LAIN')) " +
           "AND (:year IS NULL OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND YEAR(pt.targetDate) = :year)) " +
           "AND (:noInisiatif = false OR p.programInisiatifRbsi IS NULL OR TRIM(p.programInisiatifRbsi) = '') " +
           "AND (:timelineStage IS NULL OR :timelineStage = '' OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND CAST(pt.stage AS string) = :timelineStage " +
           "AND (:timelineYear IS NULL OR YEAR(pt.targetDate) = :timelineYear) " +
           "AND ((:timelineFromMonth IS NULL AND :timelineToMonth IS NULL) OR " +
           "(MONTH(pt.targetDate) BETWEEN COALESCE(:timelineFromMonth, 1) AND COALESCE(:timelineToMonth, 12)))))")
    Page<PksiDocument> searchDocumentsWithFilters(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("noInisiatif") boolean noInisiatif,
            @Param("timelineStage") String timelineStage,
            @Param("timelineFromMonth") Integer timelineFromMonth,
            @Param("timelineToMonth") Integer timelineToMonth,
            @Param("timelineYear") Integer timelineYear,
            Pageable pageable);

    /**
     * Search PKSI documents filtered by user department with year, noInisiatif, and timeline filter.
     * - year: filter by year extracted from trn_pksi_timeline table
     * - noInisiatif: if true, only return documents with null/empty program_inisiatif_rbsi
     * - timelineStage: filter by timeline stage (USREQ, SIT, UAT, etc.)
     * - timelineFromMonth: filter by month range start (1-12)
     * - timelineToMonth: filter by month range end (1-12)
     * - timelineYear: filter by specific year for timeline date
     */
    @SuppressWarnings("java:S2077") // CONCAT uses internal UUID, not user input
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u LEFT JOIN p.aplikasi a LEFT JOIN a.skpa s WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status " +
           "OR (:status = 'DISETUJUI' AND CAST(p.status AS string) = 'DIKERJAKAN_DENGAN_CARA_LAIN')) " +
           "AND (:year IS NULL OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND YEAR(pt.targetDate) = :year)) " +
           "AND (:noInisiatif = false OR p.programInisiatifRbsi IS NULL OR TRIM(p.programInisiatifRbsi) = '') " +
           "AND (:timelineStage IS NULL OR :timelineStage = '' OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND CAST(pt.stage AS string) = :timelineStage " +
           "AND (:timelineYear IS NULL OR YEAR(pt.targetDate) = :timelineYear) " +
           "AND ((:timelineFromMonth IS NULL AND :timelineToMonth IS NULL) OR " +
           "(MONTH(pt.targetDate) BETWEEN COALESCE(:timelineFromMonth, 1) AND COALESCE(:timelineToMonth, 12))))) " +
           "AND ((s IS NOT NULL AND UPPER(s.kodeSkpa) = UPPER(:userDepartment)) OR " +
           "EXISTS (SELECT 1 FROM MstSkpa skpa WHERE UPPER(skpa.kodeSkpa) = UPPER(:userDepartment) AND p.picSatker LIKE CONCAT('%', CAST(skpa.id AS string), '%')))")
    Page<PksiDocument> searchDocumentsByDepartmentWithFilters(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("noInisiatif") boolean noInisiatif,
            @Param("timelineStage") String timelineStage,
            @Param("timelineFromMonth") Integer timelineFromMonth,
            @Param("timelineToMonth") Integer timelineToMonth,
            @Param("timelineYear") Integer timelineYear,
            @Param("userDepartment") String userDepartment,
            Pageable pageable);

    /**
     * Count PKSI documents with optional year, noInisiatif, and timeline filters.
     * - year: filter by year extracted from trn_pksi_timeline table
     * - noInisiatif: if true, only return documents with null/empty program_inisiatif_rbsi
     * - timelineStage: filter by timeline stage (USREQ, SIT, UAT, etc.)
     * - timelineFromMonth: filter by month range start (1-12)
     * - timelineToMonth: filter by month range end (1-12)
     * - timelineYear: filter by specific year for timeline date
     */
    @Query("SELECT COUNT(DISTINCT p) FROM PksiDocument p WHERE " +
           "(:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status " +
           "OR (:status = 'DISETUJUI' AND CAST(p.status AS string) = 'DIKERJAKAN_DENGAN_CARA_LAIN')) " +
           "AND (:year IS NULL OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND YEAR(pt.targetDate) = :year)) " +
           "AND (:noInisiatif = false OR p.programInisiatifRbsi IS NULL OR TRIM(p.programInisiatifRbsi) = '') " +
           "AND (:timelineStage IS NULL OR :timelineStage = '' OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND CAST(pt.stage AS string) = :timelineStage " +
           "AND (:timelineYear IS NULL OR YEAR(pt.targetDate) = :timelineYear) " +
           "AND ((:timelineFromMonth IS NULL AND :timelineToMonth IS NULL) OR " +
           "(MONTH(pt.targetDate) BETWEEN COALESCE(:timelineFromMonth, 1) AND COALESCE(:timelineToMonth, 12)))))")
    long countByStatusYearAndNoInisiatif(
            @Param("status") String status,
            @Param("year") Integer year,
            @Param("noInisiatif") boolean noInisiatif,
            @Param("timelineStage") String timelineStage,
            @Param("timelineFromMonth") Integer timelineFromMonth,
            @Param("timelineToMonth") Integer timelineToMonth,
            @Param("timelineYear") Integer timelineYear);

    // ==================== NESTED PKSI QUERIES ====================

    /**
     * Find available parent PKSI candidates for nesting (optimized with projection).
     * Returns only id, nama_pksi, and nama_aplikasi for dropdown performance.
     * Filters:
     * - Status DISETUJUI
     * - Not already a child of another PKSI
     * - Has timeline in the specified year
     *
     * @param year the year to filter by (based on timeline target_date)
     * @param excludeId the PKSI id to exclude (the document being edited)
     */
    @Query("SELECT new com.pcs8.orientasi.domain.dto.response.ParentPksiSummary(" +
           "p.id, p.namaPksi, " +
           "CASE WHEN a.namaAplikasi IS NULL THEN '' ELSE a.namaAplikasi END) " +
           "FROM PksiDocument p " +
           "LEFT JOIN p.aplikasi a " +
           "WHERE p.status = 'DISETUJUI' " +
           "AND p.parentPksi IS NULL " +
           "AND (:excludeId IS NULL OR p.id <> :excludeId) " +
           "AND (:year IS NULL OR " +
           "EXISTS (SELECT 1 FROM PksiTimeline pt WHERE pt.pksiDocument.id = p.id AND YEAR(pt.targetDate) = :year)) " +
           "ORDER BY p.namaPksi ASC")
    List<ParentPksiSummary> findAvailableParentPksi(
            @Param("year") Integer year,
            @Param("excludeId") UUID excludeId);

    /**
     * Find all child PKSI documents for a given parent PKSI.
     */
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user WHERE p.parentPksi.id = :parentId")
    List<PksiDocument> findChildPksiByParentId(@Param("parentId") UUID parentId);

    /**
     * Count child PKSI documents for a given parent PKSI.
     */
    @Query("SELECT COUNT(p) FROM PksiDocument p WHERE p.parentPksi.id = :parentId")
    long countChildPksiByParentId(@Param("parentId") UUID parentId);

    /**
     * Find PKSI document by ID with parent PKSI eagerly fetched.
     */
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.parentPksi pp " +
           "LEFT JOIN FETCH p.inisiatif ini LEFT JOIN FETCH ini.group " +
           "LEFT JOIN FETCH p.inisiatifGroup WHERE p.id = :id")
    Optional<PksiDocument> findByIdWithParent(@Param("id") UUID id);
}
