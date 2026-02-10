package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Rbsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RbsiRepository extends JpaRepository<Rbsi, UUID> {

    Optional<Rbsi> findByPeriode(String periode);

    boolean existsByPeriode(String periode);
}
