package com.microstock.masterdata.web.dto;

import jakarta.validation.constraints.NotNull;

/** Body for activate/deactivate toggles on master data. */
public record ActiveRequest(@NotNull Boolean active) {
}
