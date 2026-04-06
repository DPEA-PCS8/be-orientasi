package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiChangelog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
