package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.Fs2ChangelogResponse;
import com.pcs8.orientasi.domain.entity.Fs2Document;
import com.pcs8.orientasi.domain.entity.MstUser;

import java.util.List;
import java.util.UUID;

public interface Fs2ChangelogService {

    /**
     * Get all changelogs for an FS2 document
     */
    List<Fs2ChangelogResponse> getChangelogsByFs2Id(UUID fs2DocumentId);

    /**
     * Track changes between old and new FS2 document
     */
    void trackChanges(Fs2Document fs2Document, Fs2Document oldDocument, MstUser updatedBy);

    /**
     * Count total changelogs for an FS2 document
     */
    long countChangelogsByFs2Id(UUID fs2DocumentId);
}
