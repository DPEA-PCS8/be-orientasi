package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstBidang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstBidangRepository extends JpaRepository<MstBidang, UUID> {

    Optional<MstBidang> findByKodeBidang(String kodeBidang);

    boolean existsByKodeBidang(String kodeBidang);

    List<MstBidang> findAllByOrderByKodeBidangAsc();
}
