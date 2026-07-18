package com.microstock.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base for integration tests. Runs against the local compose Postgres using a
 * dedicated {@code msamp_test} database (see application-test.yml). Flyway builds
 * the schema; tests create UUID-suffixed users so runs never collide.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}
