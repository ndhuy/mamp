package com.microstock.masterdata.web.dto;

import com.microstock.common.domain.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CaptureDeviceRequest(
        @NotBlank @Size(max = 120) String brand,
        @NotBlank @Size(max = 120) String model,
        @NotNull DeviceType deviceType,
        String mount,
        String serialNumber,
        String notes) {
}
