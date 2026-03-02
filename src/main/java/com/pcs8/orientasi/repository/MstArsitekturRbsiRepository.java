package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstArsitekturRbsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstArsitekturRbsiRepository extends JpaRepository<MstArsitekturRbsi, UUID> {

    List<MstArsitekturRbsi> findByRbsiIdOrderByCreatedAtAsc(UUID rbsiId);

    @Query("SELECT a FROM MstArsitekturRbsi a " +
           "LEFT JOIN FETCH a.rbsi " +
           "LEFT JOIN FETCH a.subKategori " +
           "LEFT JOIN FETCH a.aplikasiBaseline " +
           "LEFT JOIN FETCH a.aplikasiTarget " +
           "LEFT JOIN FETCH a.inisiatif i " +
           "LEFT JOIN FETCH i.program " +
           "LEFT JOIN FETCH a.skpa " +
           "WHERE a.rbsi.id = :rbsiId " +
           "ORDER BY a.createdAt ASC")
    List<MstArsitekturRbsi> findByRbsiIdWithRelations(@Param("rbsiId") UUID rbsiId);

    @Query("SELECT a FROM MstArsitekturRbsi a " +
           "LEFT JOIN FETCH a.rbsi " +
           "LEFT JOIN FETCH a.subKategori " +
           "LEFT JOIN FETCH a.aplikasiBaseline " +
           "LEFT JOIN FETCH a.aplikasiTarget " +
           "LEFT JOIN FETCH a.inisiatif i " +
           "LEFT JOIN FETCH i.program " +
           "LEFT JOIN FETCH a.skpa " +
           "WHERE a.id = :id")
    Optional<MstArsitekturRbsi> findByIdWithRelations(@Param("id") UUID id);

    void deleteByRbsiId(UUID rbsiId);
}
