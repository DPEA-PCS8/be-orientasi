package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstAplikasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstAplikasiRepository extends JpaRepository<MstAplikasi, UUID> {

    Optional<MstAplikasi> findByKodeAplikasi(String kodeAplikasi);

    boolean existsByKodeAplikasi(String kodeAplikasi);

    List<MstAplikasi> findAllByOrderByKodeAplikasiAsc();
}
