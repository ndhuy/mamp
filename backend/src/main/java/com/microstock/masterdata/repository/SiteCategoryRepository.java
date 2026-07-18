package com.microstock.masterdata.repository;

import com.microstock.masterdata.domain.SiteCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteCategoryRepository extends JpaRepository<SiteCategory, UUID> {

    List<SiteCategory> findByStockSiteIdOrderByNameAsc(UUID stockSiteId);

    List<SiteCategory> findByStockSiteIdAndActiveTrueOrderByNameAsc(UUID stockSiteId);

    boolean existsByStockSiteIdAndNameIgnoreCase(UUID stockSiteId, String name);

    boolean existsByStockSiteIdAndNameIgnoreCaseAndIdNot(UUID stockSiteId, String name, UUID id);
}
