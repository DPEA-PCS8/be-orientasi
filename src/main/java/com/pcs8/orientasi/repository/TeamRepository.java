package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<MstTeam, UUID> {

    /**
     * Find all teams with PIC eagerly loaded
     */
    @Query("SELECT DISTINCT t FROM MstTeam t LEFT JOIN FETCH t.pic ORDER BY t.createdAt DESC")
    List<MstTeam> findAllWithPic();

    /**
     * Find team by ID with PIC and members eagerly loaded
     */
    @Query("SELECT t FROM MstTeam t " +
           "LEFT JOIN FETCH t.pic " +
           "LEFT JOIN FETCH t.teamMembers tm " +
           "LEFT JOIN FETCH tm.user " +
           "WHERE t.id = :id")
    Optional<MstTeam> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find all teams with full details (PIC and members)
     */
    @Query("SELECT DISTINCT t FROM MstTeam t " +
           "LEFT JOIN FETCH t.pic " +
           "LEFT JOIN FETCH t.teamMembers tm " +
           "LEFT JOIN FETCH tm.user " +
           "ORDER BY t.createdAt DESC")
    List<MstTeam> findAllWithDetails();

    /**
     * Check if team name already exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if team name already exists excluding specific team
     */
    @Query("SELECT COUNT(t) > 0 FROM MstTeam t WHERE LOWER(t.name) = LOWER(:name) AND t.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);

    /**
     * Find teams where user is PIC
     */
    @Query("SELECT t FROM MstTeam t WHERE t.pic.uuid = :userUuid")
    List<MstTeam> findByPicUuid(@Param("userUuid") UUID userUuid);

    /**
     * Find teams where user is a member
     */
    @Query("SELECT DISTINCT t FROM MstTeam t " +
           "JOIN t.teamMembers tm " +
           "WHERE tm.user.uuid = :userUuid")
    List<MstTeam> findByMemberUuid(@Param("userUuid") UUID userUuid);
}
