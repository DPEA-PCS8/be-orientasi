package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstRolePermissionRepository extends JpaRepository<MstRolePermission, UUID> {

    @Query("SELECT rp FROM MstRolePermission rp " +
           "JOIN FETCH rp.role " +
           "JOIN FETCH rp.menu " +
           "WHERE rp.role.id = :roleId")
    List<MstRolePermission> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT rp FROM MstRolePermission rp " +
           "JOIN FETCH rp.role " +
           "JOIN FETCH rp.menu " +
           "WHERE rp.menu.id = :menuId")
    List<MstRolePermission> findByMenuId(@Param("menuId") UUID menuId);

    @Query("SELECT rp FROM MstRolePermission rp " +
           "JOIN FETCH rp.role " +
           "JOIN FETCH rp.menu " +
           "WHERE rp.role.id = :roleId AND rp.menu.id = :menuId")
    Optional<MstRolePermission> findByRoleIdAndMenuId(@Param("roleId") UUID roleId, @Param("menuId") UUID menuId);

    @Modifying
    @Query("DELETE FROM MstRolePermission rp WHERE rp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);

    @Modifying
    @Query("DELETE FROM MstRolePermission rp WHERE rp.role.id = :roleId AND rp.menu.id = :menuId")
    void deleteByRoleIdAndMenuId(@Param("roleId") UUID roleId, @Param("menuId") UUID menuId);

    @Query("SELECT rp FROM MstRolePermission rp " +
           "JOIN FETCH rp.role " +
           "JOIN FETCH rp.menu m " +
           "WHERE rp.role.id = :roleId AND m.menuCode = :menuCode")
    Optional<MstRolePermission> findByRoleIdAndMenuCode(@Param("roleId") UUID roleId, @Param("menuCode") String menuCode);

    @Query("SELECT rp FROM MstRolePermission rp " +
           "JOIN FETCH rp.role r " +
           "JOIN FETCH rp.menu m " +
           "WHERE r.roleName = :roleName AND m.menuCode = :menuCode")
    Optional<MstRolePermission> findByRoleNameAndMenuCode(@Param("roleName") String roleName, @Param("menuCode") String menuCode);
}