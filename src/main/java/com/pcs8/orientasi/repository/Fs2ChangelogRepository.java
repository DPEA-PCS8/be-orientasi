package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Fs2Changelog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Fs2ChangelogRepository extends JpaRepository<Fs2Changelog, UUID> {

    List<Fs2Changelog> findByFs2DocumentIdOrderByCreatedAtDesc(UUID fs2DocumentId);

    long countByFs2DocumentId(UUID fs2DocumentId);

    @Modifying
    void deleteByFs2DocumentId(UUID fs2DocumentId);
}
