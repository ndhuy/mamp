package com.microstock.common.domain;

/** Lifecycle of a submission record at one stock site (PRD 6.12). */
public enum SubmissionStatus {
    NOT_SUBMITTED,
    SUBMITTED,
    IN_REVIEW,
    ACCEPTED,
    REJECTED,
    RESUBMIT_REQUIRED,
    RESUBMITTED,
    REMOVED
}
