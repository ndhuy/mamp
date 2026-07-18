package com.microstock.masterdata.repository;

import com.microstock.masterdata.domain.CaptureDevice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaptureDeviceRepository extends JpaRepository<CaptureDevice, UUID> {

    boolean existsByNormalizedKey(String normalizedKey);

    boolean existsByNormalizedKeyAndIdNot(String normalizedKey, UUID id);

    List<CaptureDevice> findAllByOrderByBrandAscModelAsc();

    List<CaptureDevice> findByActiveTrueOrderByBrandAscModelAsc();
}
