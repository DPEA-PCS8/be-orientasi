package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.PksiDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PksiDocumentRepository extends JpaRepository<PksiDocument, UUID> {
    
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user WHERE p.user.uuid = :userUuid")
    List<PksiDocument> findByUserUuid(@Param("userUuid") UUID userUuid);
    
    List<PksiDocument> findByStatus(PksiDocument.DocumentStatus status);
    
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user")
    List<PksiDocument> findAllWithUser();
    
    @Query("SELECT p FROM PksiDocument p LEFT JOIN FETCH p.user WHERE p.id = :id")
    Optional<PksiDocument> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT p FROM PksiDocument p LEFT JOIN p.user u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.namaPksi) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.picSatker) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR p.status = :status)")
    Page<PksiDocument> searchDocuments(
            @Param("search") String search, 
            @Param("status") String status, 
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM PksiDocument p WHERE p.status = :status")
    long countByStatus(@Param("status") PksiDocument.DocumentStatus status);
}
