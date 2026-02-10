package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstRbsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RbsiRepository extends JpaRepository<MstRbsi, UUID> {

    @Query("SELECT r FROM MstRbsi r WHERE r.isActive = true ORDER BY r.periode DESC")
    List<MstRbsi> findAllActive();

    Optional<MstRbsi> findByPeriode(String periode);

    boolean existsByPeriode(String periode);
}
