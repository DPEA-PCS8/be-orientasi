package com.pcs8.orientasi.repository;

import com.pcs8.orientasi.domain.entity.MstMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MstMenuRepository extends JpaRepository<MstMenu, UUID> {

    Optional<MstMenu> findByMenuCode(String menuCode);

    boolean existsByMenuCode(String menuCode);

    @Query("SELECT m FROM MstMenu m WHERE m.parent IS NULL AND m.isActive = true ORDER BY m.displayOrder")
    List<MstMenu> findAllRootMenus();

    @Query("SELECT m FROM MstMenu m WHERE m.isActive = true ORDER BY m.displayOrder")
    List<MstMenu> findAllActiveMenus();

    @Query("SELECT m FROM MstMenu m LEFT JOIN FETCH m.children WHERE m.parent IS NULL AND m.isActive = true ORDER BY m.displayOrder")
    List<MstMenu> findAllRootMenusWithChildren();

    List<MstMenu> findByParentIdAndIsActiveTrue(UUID parentId);
}