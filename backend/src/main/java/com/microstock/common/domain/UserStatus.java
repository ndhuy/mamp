package com.microstock.common.domain;

/** Account status. DISABLED accounts cannot authenticate (AUTH-004, BR-019). */
public enum UserStatus {
    ACTIVE,
    DISABLED
}
