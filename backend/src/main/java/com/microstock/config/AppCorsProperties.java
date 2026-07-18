package com.microstock.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds app.cors.* settings. */
@ConfigurationProperties(prefix = "app.cors")
public record AppCorsProperties(List<String> allowedOrigins) {
}
