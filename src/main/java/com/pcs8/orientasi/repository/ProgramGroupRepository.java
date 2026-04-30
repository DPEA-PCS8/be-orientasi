package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.ProgramGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgramGroupRepository extends JpaRepository<ProgramGroup, UUID> {

    List<ProgramGroup> findByRbsiIdOrderByCreatedAtAsc(UUID rbsiId);

    @Query("SELECT COUNT(p) FROM RbsiProgram p WHERE p.programGroup.id = :groupId AND p.isDeleted = false")
    long countActiveByGroupId(@Param("groupId") UUID groupId);
}
