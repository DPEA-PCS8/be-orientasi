package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstRoleRepository extends JpaRepository<MstRole, UUID> {

    Optional<MstRole> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}
