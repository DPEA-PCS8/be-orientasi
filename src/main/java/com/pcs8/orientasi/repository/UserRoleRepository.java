package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<MstUserRole, UUID> {

    List<MstUserRole> findByUser_Uuid(UUID userUuid);

    List<MstUserRole> findByRole_Id(UUID roleId);

    @Query("SELECT ur FROM MstUserRole ur WHERE ur.user.uuid = :userUuid AND ur.role.id = :roleId")
    Optional<MstUserRole> findByUserUuidAndRoleId(@Param("userUuid") UUID userUuid, @Param("roleId") UUID roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MstUserRole ur WHERE ur.user.uuid = :userUuid AND ur.role.id = :roleId")
    void deleteByUser_UuidAndRole_Id(@Param("userUuid") UUID userUuid, @Param("roleId") UUID roleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM MstUserRole ur WHERE ur.user.uuid = :userUuid")
    void deleteByUser_Uuid(@Param("userUuid") UUID userUuid);
}
