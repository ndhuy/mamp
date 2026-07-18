package com.microstock.submission.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Selecting a target site creates one Not Submitted record (PRD 6.11). */
public record AddTargetSiteRequest(@NotNull UUID stockSiteId) {
}
