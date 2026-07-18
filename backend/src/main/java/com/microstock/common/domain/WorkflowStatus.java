package com.microstock.common.domain;

/** Media lifecycle states (PRD 6.3). */
public enum WorkflowStatus {
    DRAFT,
    EDITING,
    METADATA_PENDING,
    READY,        // "Ready for Upload"
    UPLOADING,
    COMPLETED,
    ARCHIVED
}
