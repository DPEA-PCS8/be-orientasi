package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstUserRepository extends JpaRepository<MstUser, UUID> {

    Optional<MstUser> findByUsername(String username);

    boolean existsByUsername(String username);
}