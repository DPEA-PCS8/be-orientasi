package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiChangelog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PksiChangelogRepository extends JpaRepository<PksiChangelog, UUID> {

    /**
     * Find all changelogs for a specific PKSI document, ordered by created_at descending
     */
    @Query("SELECT c FROM PksiChangelog c " +
           "LEFT JOIN FETCH c.updatedBy " +
           "WHERE c.pksiDocument.id = :pksiDocumentId " +
           "ORDER BY c.createdAt DESC")
    List<PksiChangelog> findByPksiDocumentIdOrderByCreatedAtDesc(@Param("pksiDocumentId") UUID pksiDocumentId);

    /**
     * Count changelogs for a specific PKSI document
     */
    long countByPksiDocumentId(UUID pksiDocumentId);

    /**
     * Delete all changelogs for a specific PKSI document
     */
    void deleteByPksiDocumentId(UUID pksiDocumentId);

    /**
     * Find the last progress change for each PKSI document before a given date
     * Used for historical snapshot of progress status
     */
    @Query("SELECT c FROM PksiChangelog c " +
           "WHERE c.fieldName = 'progress' " +
           "AND c.createdAt <= :beforeDate " +
           "AND c.createdAt = (SELECT MAX(c2.createdAt) FROM PksiChangelog c2 " +
           "                   WHERE c2.pksiDocument.id = c.pksiDocument.id " +
           "                   AND c2.fieldName = 'progress' " +
           "                   AND c2.createdAt <= :beforeDate)")
    List<PksiChangelog> findLastProgressChangeBeforeDate(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Find all progress changes within a date range
     */
    @Query("SELECT c FROM PksiChangelog c " +
           "WHERE c.fieldName = 'progress' " +
           "AND c.createdAt >= :startDate " +
           "AND c.createdAt <= :endDate " +
           "ORDER BY c.createdAt DESC")
    List<PksiChangelog> findProgressChangesBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
