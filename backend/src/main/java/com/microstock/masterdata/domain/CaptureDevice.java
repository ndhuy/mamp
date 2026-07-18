package com.microstock.masterdata.domain;

import com.microstock.common.domain.DeviceType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Global master data: a camera, phone, drone, etc. (PRD 6.6). */
@Entity
@Table(name = "capture_device")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaptureDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    /** Normalized "brand|model" — enforces VAL-010 uniqueness. */
    @Column(name = "normalized_key", nullable = false, unique = true)
    private String normalizedKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private DeviceType deviceType;

    private String mount;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CaptureDevice(String brand, String model, String normalizedKey, DeviceType deviceType) {
        this.brand = brand;
        this.model = model;
        this.normalizedKey = normalizedKey;
        this.deviceType = deviceType;
    }
}
