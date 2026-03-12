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
    
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user WHERE p.user.uuid = :userUuid")
    List<PksiDocument> findByUserUuid(@Param("userUuid") UUID userUuid);
    
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user WHERE p.user.uuid = :userUuid")
    List<PksiDocument> findByUserUuidWithAplikasi(@Param("userUuid") UUID userUuid);
    
    List<PksiDocument> findByStatus(PksiDocument.DocumentStatus status);
    
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user")
    List<PksiDocument> findAllWithUser();
    
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user WHERE p.id = :id")
    Optional<PksiDocument> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status)")
    Page<PksiDocument> searchDocuments(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status, 
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM PksiDocument p WHERE p.status = :status")
    long countByStatus(@Param("status") PksiDocument.DocumentStatus status);

    /**
     * Search PKSI documents filtered by user department.
     * Matches documents where:
     * 1. aplikasi.skpa.kodeSkpa matches userDepartment, OR
     * 2. picSatker field contains SKPA ID whose kodeSkpa matches userDepartment
     * 
     * Note: CONCAT is used with internal database UUID values (skpa.id), not user input,
     * so this is safe from SQL injection.
     */
    @SuppressWarnings("java:S2077") // CONCAT uses internal UUID, not user input
    @Query("SELECT DISTINCT p FROM PksiDocument p LEFT JOIN FETCH p.user u LEFT JOIN p.aplikasi a LEFT JOIN a.skpa s WHERE " +
           "(:searchPattern IS NULL OR :searchPattern = '' OR " +
           "LOWER(p.namaPksi) LIKE :searchPattern OR " +
           "LOWER(u.fullName) LIKE :searchPattern OR " +
           "LOWER(p.picSatker) LIKE :searchPattern) " +
           "AND (:status IS NULL OR :status = '' OR CAST(p.status AS string) = :status) " +
           "AND (:userDepartment IS NULL OR :userDepartment = '' OR " +
           "UPPER(s.kodeSkpa) = UPPER(:userDepartment) OR " +
           "EXISTS (SELECT 1 FROM MstSkpa skpa WHERE UPPER(skpa.kodeSkpa) = UPPER(:userDepartment) AND p.picSatker LIKE CONCAT('%', CAST(skpa.id AS string), '%')))")
    Page<PksiDocument> searchDocumentsByDepartment(
            @Param("searchPattern") String searchPattern, 
            @Param("status") String status,
            @Param("userDepartment") String userDepartment,
            Pageable pageable);
}
