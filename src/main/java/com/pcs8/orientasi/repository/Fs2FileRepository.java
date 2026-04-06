package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Fs2File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Fs2FileRepository extends JpaRepository<Fs2File, UUID> {
    
    List<Fs2File> findByFs2DocumentId(UUID fs2Id);
    
    List<Fs2File> findByFs2DocumentIdOrderByCreatedAtDesc(UUID fs2Id);
    
    void deleteByFs2DocumentId(UUID fs2Id);
    
    List<Fs2File> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    void deleteBySessionId(String sessionId);
}
