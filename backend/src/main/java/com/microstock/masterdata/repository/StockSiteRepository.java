package com.microstock.masterdata.repository;

import com.microstock.masterdata.domain.StockSite;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockSiteRepository extends JpaRepository<StockSite, UUID> {

    boolean existsByNormalizedName(String normalizedName);

    boolean existsByNormalizedNameAndIdNot(String normalizedName, UUID id);

    List<StockSite> findAllByOrderByDisplayOrderAscNameAsc();

    List<StockSite> findByActiveTrueOrderByDisplayOrderAscNameAsc();
}
