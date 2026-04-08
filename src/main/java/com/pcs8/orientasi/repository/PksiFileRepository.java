package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PksiFileRepository extends JpaRepository<PksiFile, UUID> {
    
    List<PksiFile> findByPksiDocumentId(UUID pksiId);
    
    List<PksiFile> findByPksiDocumentIdOrderByCreatedAtDesc(UUID pksiId);
    
    void deleteByPksiDocumentId(UUID pksiId);
    
    List<PksiFile> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    void deleteBySessionId(String sessionId);

    // ==================== VERSIONING QUERIES ====================

    /**
     * Find all files for a PKSI document by file type, ordered by version descending (latest first)
     */
    List<PksiFile> findByPksiDocumentIdAndFileTypeOrderByVersionDesc(UUID pksiId, String fileType);

    /**
     * Find the latest version file for a PKSI document by file type
     */
    Optional<PksiFile> findFirstByPksiDocumentIdAndFileTypeOrderByVersionDesc(UUID pksiId, String fileType);

    /**
     * Find all files in a file group (same logical file, different versions)
     */
    List<PksiFile> findByFileGroupIdOrderByVersionDesc(UUID fileGroupId);

    /**
     * Find the latest version in a file group
     */
    Optional<PksiFile> findFirstByFileGroupIdOrderByVersionDesc(UUID fileGroupId);

    /**
     * Get the maximum version number for a specific file type in a PKSI document
     */
    @Query("SELECT COALESCE(MAX(f.version), 0) FROM PksiFile f WHERE f.pksiDocument.id = :pksiId AND f.fileType = :fileType")
    Integer findMaxVersionByPksiIdAndFileType(@Param("pksiId") UUID pksiId, @Param("fileType") String fileType);

    /**
     * Find all latest version files for a PKSI document (one per file type)
     */
    @Query("SELECT f FROM PksiFile f WHERE f.pksiDocument.id = :pksiId AND f.version = " +
           "(SELECT MAX(f2.version) FROM PksiFile f2 WHERE f2.pksiDocument.id = :pksiId AND f2.fileType = f.fileType)")
    List<PksiFile> findLatestVersionFilesByPksiId(@Param("pksiId") UUID pksiId);

    /**
     * Find file history for a specific file type (all versions)
     */
    @Query("SELECT f FROM PksiFile f WHERE f.pksiDocument.id = :pksiId AND f.fileType = :fileType ORDER BY f.version DESC")
    List<PksiFile> findFileHistoryByPksiIdAndFileType(@Param("pksiId") UUID pksiId, @Param("fileType") String fileType);
}
