package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstRoleRepository extends JpaRepository<MstRole, UUID> {

    Optional<MstRole> findByRoleName(String roleName);

    // Case-insensitive search for role by name
    @Query("SELECT r FROM MstRole r WHERE LOWER(r.roleName) = LOWER(:roleName)")
    Optional<MstRole> findByRoleNameIgnoreCase(@Param("roleName") String roleName);

    boolean existsByRoleName(String roleName);
}
