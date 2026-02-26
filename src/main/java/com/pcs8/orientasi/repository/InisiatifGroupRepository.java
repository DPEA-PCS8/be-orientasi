package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.InisiatifGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InisiatifGroupRepository extends JpaRepository<InisiatifGroup, UUID> {

    List<InisiatifGroup> findByRbsiIdOrderByCreatedAtAsc(UUID rbsiId);
}
