package com.microstock.masterdata.repository;

import com.microstock.masterdata.domain.Lens;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LensRepository extends JpaRepository<Lens, UUID> {

    boolean existsByNormalizedKey(String normalizedKey);

    boolean existsByNormalizedKeyAndIdNot(String normalizedKey, UUID id);

    List<Lens> findAllByOrderByBrandAscModelAsc();

    List<Lens> findByActiveTrueOrderByBrandAscModelAsc();
}
