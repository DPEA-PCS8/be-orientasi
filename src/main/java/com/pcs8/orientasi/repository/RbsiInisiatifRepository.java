package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.RbsiInisiatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RbsiInisiatifRepository extends JpaRepository<RbsiInisiatif, Long> {

    List<RbsiInisiatif> findByProgramIdAndTahunOrderByNomorInisiatifAsc(Long programId, Integer tahun);

    Optional<RbsiInisiatif> findByProgramIdAndTahunAndNomorInisiatif(Long programId, Integer tahun, String nomorInisiatif);

    boolean existsByProgramIdAndTahunAndNomorInisiatif(Long programId, Integer tahun, String nomorInisiatif);
}
