package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.InisiatifGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InisiatifGroupRepository extends JpaRepository<InisiatifGroup, UUID> {

    List<InisiatifGroup> findByRbsiIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID rbsiId);

    @Query("SELECT COUNT(i) FROM RbsiInisiatif i WHERE i.group.id = :groupId AND i.isDeleted = false")
    long countActiveByGroupId(@Param("groupId") UUID groupId);
}
