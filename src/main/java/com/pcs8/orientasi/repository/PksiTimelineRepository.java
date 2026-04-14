package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository untuk PksiTimeline
 */
@Repository
public interface PksiTimelineRepository extends JpaRepository<PksiTimeline, UUID> {

    /**
     * Find all timelines for a specific PKSI document
     */
    List<PksiTimeline> findByPksiDocumentIdOrderByStageAscPhaseAsc(UUID pksiDocumentId);

    /**
     * Delete all timelines for a specific PKSI document
     */
    @Modifying
    @Query("DELETE FROM PksiTimeline t WHERE t.pksiDocument.id = :pksiId")
    void deleteByPksiDocumentId(@Param("pksiId") UUID pksiId);
}
