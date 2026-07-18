package com.microstock.masterdata.repository;

import com.microstock.masterdata.domain.RejectionCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectionCategoryRepository extends JpaRepository<RejectionCategory, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    List<RejectionCategory> findAllByOrderByNameAsc();

    List<RejectionCategory> findByActiveTrueOrderByNameAsc();
}
