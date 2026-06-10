package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.RbsiProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RbsiProgramRepository extends JpaRepository<RbsiProgram, UUID> {

    @Query("SELECT p FROM RbsiProgram p WHERE p.programGroup.rbsi.id = :rbsiId AND p.isDeleted = false ORDER BY p.nomorProgram ASC")
    List<RbsiProgram> findByRbsiIdAndIsDeletedFalseOrderByNomorProgramAsc(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT p FROM RbsiProgram p WHERE p.programGroup.rbsi.id = :rbsiId AND p.tahun = :tahun AND p.isDeleted = false ORDER BY p.nomorProgram ASC")
    List<RbsiProgram> findByRbsiIdAndTahunAndIsDeletedFalseOrderByNomorProgramAsc(@Param("rbsiId") UUID rbsiId, @Param("tahun") Integer tahun);

    @Query("SELECT p FROM RbsiProgram p WHERE p.programGroup.rbsi.id = :rbsiId AND p.tahun = :tahun AND p.nomorProgram = :nomorProgram AND p.isDeleted = false")
    Optional<RbsiProgram> findByRbsiIdAndTahunAndNomorProgramAndIsDeletedFalse(
            @Param("rbsiId") UUID rbsiId,
            @Param("tahun") Integer tahun,
            @Param("nomorProgram") String nomorProgram);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM RbsiProgram p WHERE p.programGroup.rbsi.id = :rbsiId AND p.tahun = :tahun AND p.nomorProgram = :nomorProgram AND p.isDeleted = false")
    boolean existsByRbsiIdAndTahunAndNomorProgramAndIsDeletedFalse(
            @Param("rbsiId") UUID rbsiId,
            @Param("tahun") Integer tahun,
            @Param("nomorProgram") String nomorProgram);

    @Query("SELECT DISTINCT p FROM RbsiProgram p LEFT JOIN FETCH p.inisiatifs i WHERE p.programGroup.rbsi.id = :rbsiId AND p.tahun = :tahun AND p.isDeleted = false ORDER BY p.nomorProgram ASC")
    List<RbsiProgram> findByRbsiIdAndTahunWithInisiatifs(@Param("rbsiId") UUID rbsiId, @Param("tahun") Integer tahun);

    @Query("SELECT DISTINCT p FROM RbsiProgram p LEFT JOIN FETCH p.inisiatifs i WHERE p.programGroup.rbsi.id = :rbsiId AND p.isDeleted = false ORDER BY p.tahun DESC, p.nomorProgram ASC")
    List<RbsiProgram> findByRbsiIdWithInisiatifs(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT MAX(p.tahun) FROM RbsiProgram p WHERE p.programGroup.rbsi.id = :rbsiId AND p.isDeleted = false")
    Integer findMaxTahunByRbsiId(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT DISTINCT p.tahun FROM RbsiProgram p WHERE p.programGroup.rbsi.id = :rbsiId AND p.isDeleted = false ORDER BY p.tahun DESC")
    List<Integer> findDistinctTahunByRbsiId(@Param("rbsiId") UUID rbsiId);
}
