package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstVariableRepository extends JpaRepository<MstVariable, UUID> {

    List<MstVariable> findByKategoriAndIsActiveTrueOrderByUrutanAscNamaAsc(String kategori);

    Optional<MstVariable> findByKategoriAndKode(String kategori, String kode);

    List<MstVariable> findByIsActiveTrueOrderByKategoriAscUrutanAscNamaAsc();

    boolean existsByKategoriAndKode(String kategori, String kode);
}
