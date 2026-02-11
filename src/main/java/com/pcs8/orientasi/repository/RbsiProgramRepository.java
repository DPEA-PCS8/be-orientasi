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

    List<RbsiProgram> findByRbsiIdOrderByNomorProgramAsc(UUID rbsiId);

    List<RbsiProgram> findByRbsiIdAndTahunOrderByNomorProgramAsc(UUID rbsiId, Integer tahun);

    Optional<RbsiProgram> findByRbsiIdAndTahunAndNomorProgram(UUID rbsiId, Integer tahun, String nomorProgram);

    boolean existsByRbsiIdAndTahunAndNomorProgram(UUID rbsiId, Integer tahun, String nomorProgram);

    @Query("SELECT DISTINCT p FROM RbsiProgram p LEFT JOIN FETCH p.inisiatifs i WHERE p.rbsi.id = :rbsiId AND p.tahun = :tahun ORDER BY p.nomorProgram ASC")
    List<RbsiProgram> findByRbsiIdAndTahunWithInisiatifs(@Param("rbsiId") UUID rbsiId, @Param("tahun") Integer tahun);

    @Query("SELECT DISTINCT p FROM RbsiProgram p LEFT JOIN FETCH p.inisiatifs i WHERE p.rbsi.id = :rbsiId ORDER BY p.tahun DESC, p.nomorProgram ASC")
    List<RbsiProgram> findByRbsiIdWithInisiatifs(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT MAX(p.tahun) FROM RbsiProgram p WHERE p.rbsi.id = :rbsiId")
    Integer findMaxTahunByRbsiId(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT DISTINCT p.tahun FROM RbsiProgram p WHERE p.rbsi.id = :rbsiId ORDER BY p.tahun DESC")
    List<Integer> findDistinctTahunByRbsiId(@Param("rbsiId") UUID rbsiId);
}
