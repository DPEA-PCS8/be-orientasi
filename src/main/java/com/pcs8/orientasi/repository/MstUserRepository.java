package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstUserRepository extends JpaRepository<MstUser, UUID> {

    Optional<MstUser> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * Find user by username with roles eagerly loaded
     */
    @Query("SELECT u FROM MstUser u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.username = :username")
    Optional<MstUser> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Find user by UUID with roles eagerly loaded
     */
    @Query("SELECT u FROM MstUser u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.uuid = :uuid")
    Optional<MstUser> findByIdWithRoles(@Param("uuid") UUID uuid);

    /**
     * Find all users with roles eagerly loaded
     */
    @Query("SELECT DISTINCT u FROM MstUser u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role")
    java.util.List<MstUser> findAllWithRoles();
}