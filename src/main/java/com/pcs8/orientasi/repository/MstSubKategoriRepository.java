package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstSubKategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstSubKategoriRepository extends JpaRepository<MstSubKategori, UUID> {

    Optional<MstSubKategori> findByKode(String kode);

    boolean existsByKode(String kode);

    List<MstSubKategori> findByCategoryCodeOrderByKodeAsc(String categoryCode);

    List<MstSubKategori> findAllByOrderByKodeAsc();

    @Query("SELECT DISTINCT s.categoryCode FROM MstSubKategori s ORDER BY s.categoryCode ASC")
    List<String> findDistinctCategoryCodes();
}
