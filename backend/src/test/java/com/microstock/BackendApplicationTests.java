package com.microstock;

import com.microstock.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/** Smoke test: boots the full context + Flyway migration against Postgres. */
class BackendApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Verifies Spring context + Flyway migration + JPA entity validation.
    }
}
