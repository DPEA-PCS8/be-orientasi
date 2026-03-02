package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstSkpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstSkpaRepository extends JpaRepository<MstSkpa, UUID> {

    Optional<MstSkpa> findByKodeSkpa(String kodeSkpa);

    boolean existsByKodeSkpa(String kodeSkpa);

    List<MstSkpa> findAllByOrderByKodeSkpaAsc();
}
