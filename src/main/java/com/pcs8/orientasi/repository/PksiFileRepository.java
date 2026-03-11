package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PksiFileRepository extends JpaRepository<PksiFile, UUID> {
    
    List<PksiFile> findByPksiDocumentId(UUID pksiId);
    
    List<PksiFile> findByPksiDocumentIdOrderByCreatedAtDesc(UUID pksiId);
    
    void deleteByPksiDocumentId(UUID pksiId);
    
    List<PksiFile> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    void deleteBySessionId(String sessionId);
}
