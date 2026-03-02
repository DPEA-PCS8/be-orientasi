package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PksiDocumentRepository extends JpaRepository<PksiDocument, UUID> {
    
    List<PksiDocument> findByUserUuid(UUID userUuid);
    
    List<PksiDocument> findByStatus(PksiDocument.DocumentStatus status);
}
