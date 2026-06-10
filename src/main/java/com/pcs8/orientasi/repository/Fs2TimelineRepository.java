package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Fs2Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Fs2TimelineRepository extends JpaRepository<Fs2Timeline, UUID> {
    
    List<Fs2Timeline> findByFs2DocumentIdOrderByStageAscPhaseAsc(UUID fs2DocumentId);
    
    void deleteByFs2DocumentId(UUID fs2DocumentId);
}
