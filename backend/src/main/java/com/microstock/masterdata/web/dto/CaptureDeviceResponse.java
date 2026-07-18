package com.microstock.masterdata.web.dto;

import com.microstock.common.domain.DeviceType;
import com.microstock.masterdata.domain.CaptureDevice;
import java.util.UUID;

public record CaptureDeviceResponse(
        UUID id,
        String brand,
        String model,
        DeviceType deviceType,
        String mount,
        String serialNumber,
        String notes,
        boolean active) {

    public static CaptureDeviceResponse from(CaptureDevice d) {
        return new CaptureDeviceResponse(
                d.getId(), d.getBrand(), d.getModel(), d.getDeviceType(),
                d.getMount(), d.getSerialNumber(), d.getNotes(), d.isActive());
    }
}
