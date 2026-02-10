package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.Rbsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RbsiRepository extends JpaRepository<Rbsi, Long> {

    Optional<Rbsi> findByPeriode(String periode);

    boolean existsByPeriode(String periode);
}
