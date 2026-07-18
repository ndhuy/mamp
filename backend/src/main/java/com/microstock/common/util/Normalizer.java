package com.microstock.common.util;

/** Shared normalization for uniqueness keys and names. */
public final class Normalizer {

    private Normalizer() {}

    /** Trim, collapse repeated whitespace, lowercase. Null-safe. */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Normalized "brand|model" key for CaptureDevice/Lens uniqueness (VAL-010). */
    public static String key(String brand, String model) {
        return normalize(brand) + "|" + normalize(model);
    }
}
