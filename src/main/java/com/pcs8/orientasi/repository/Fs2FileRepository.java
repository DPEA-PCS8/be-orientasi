package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Fs2File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface Fs2FileRepository extends JpaRepository<Fs2File, UUID> {
    
    List<Fs2File> findByFs2DocumentId(UUID fs2Id);
    
    List<Fs2File> findByFs2DocumentIdOrderByCreatedAtDesc(UUID fs2Id);
    
    @Modifying
    void deleteByFs2DocumentId(UUID fs2Id);
    
    List<Fs2File> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    @Modifying
    void deleteBySessionId(String sessionId);

    // ==================== VERSIONING QUERIES ====================

    /**
     * Find all files for a F.S.2 document by file type, ordered by version descending (latest first)
     */
    List<Fs2File> findByFs2DocumentIdAndFileTypeOrderByVersionDesc(UUID fs2Id, String fileType);

    /**
     * Find the latest version file for a F.S.2 document by file type
     */
    Optional<Fs2File> findFirstByFs2DocumentIdAndFileTypeOrderByVersionDesc(UUID fs2Id, String fileType);

    /**
     * Find all files in a file group (same logical file, different versions)
     */
    List<Fs2File> findByFileGroupIdOrderByVersionDesc(UUID fileGroupId);

    /**
     * Find the latest version in a file group
     */
    Optional<Fs2File> findFirstByFileGroupIdOrderByVersionDesc(UUID fileGroupId);

    /**
     * Get the maximum version number for a specific file type in a F.S.2 document
     */
    @Query("SELECT COALESCE(MAX(f.version), 0) FROM Fs2File f WHERE f.fs2Document.id = :fs2Id AND f.fileType = :fileType")
    Integer findMaxVersionByFs2IdAndFileType(@Param("fs2Id") UUID fs2Id, @Param("fileType") String fileType);

    /**
     * Find all latest version files for a F.S.2 document (one per file type)
     */
    @Query("SELECT f FROM Fs2File f WHERE f.fs2Document.id = :fs2Id AND f.version = " +
           "(SELECT MAX(f2.version) FROM Fs2File f2 WHERE f2.fs2Document.id = :fs2Id AND f2.fileType = f.fileType)")
    List<Fs2File> findLatestVersionFilesByFs2Id(@Param("fs2Id") UUID fs2Id);

    /**
     * Find file history for a specific file type (all versions)
     */
    @Query("SELECT f FROM Fs2File f WHERE f.fs2Document.id = :fs2Id AND f.fileType = :fileType ORDER BY f.version DESC")
    List<Fs2File> findFileHistoryByFs2IdAndFileType(@Param("fs2Id") UUID fs2Id, @Param("fileType") String fileType);
}
