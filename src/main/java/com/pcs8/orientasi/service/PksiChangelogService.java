package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.PksiChangelogResponse;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.MstUser;

import java.util.List;
import java.util.UUID;

public interface PksiChangelogService {

    /**
     * Get all changelogs for a specific PKSI document
     */
    List<PksiChangelogResponse> getChangelogsByPksiId(UUID pksiDocumentId);

    /**
     * Track changes between old and new PKSI document values
     * @param pksiDocument The PKSI document being updated
     * @param oldDocument Snapshot of the document before update
     * @param updatedBy The user making the changes
     */
    void trackChanges(PksiDocument pksiDocument, PksiDocument oldDocument, MstUser updatedBy);

    /**
     * Count changelogs for a specific PKSI document
     */
    long countChangelogsByPksiId(UUID pksiDocumentId);
}
