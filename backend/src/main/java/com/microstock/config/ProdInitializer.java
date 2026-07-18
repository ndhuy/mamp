package com.microstock.config;

import com.microstock.common.domain.Role;
import com.microstock.common.util.Normalizer;
import com.microstock.masterdata.domain.RejectionCategory;
import com.microstock.masterdata.domain.SiteCategory;
import com.microstock.masterdata.domain.StockSite;
import com.microstock.masterdata.repository.RejectionCategoryRepository;
import com.microstock.masterdata.repository.SiteCategoryRepository;
import com.microstock.masterdata.repository.StockSiteRepository;
import com.microstock.user.domain.User;
import com.microstock.user.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * First-run bootstrap for production: creates the initial administrator from
 * environment variables and seeds baseline global master data. No demo data.
 * (The dev DataSeeder is disabled under the "prod" profile.)
 */
@Component
@Profile("prod")
public class ProdInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockSiteRepository stockSiteRepository;
    private final SiteCategoryRepository siteCategoryRepository;
    private final RejectionCategoryRepository rejectionCategoryRepository;

    @Value("${INITIAL_ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${INITIAL_ADMIN_EMAIL:admin@example.com}")
    private String adminEmail;

    @Value("${INITIAL_ADMIN_PASSWORD:}")
    private String adminPassword;

    public ProdInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StockSiteRepository stockSiteRepository,
            SiteCategoryRepository siteCategoryRepository,
            RejectionCategoryRepository rejectionCategoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.stockSiteRepository = stockSiteRepository;
        this.siteCategoryRepository = siteCategoryRepository;
        this.rejectionCategoryRepository = rejectionCategoryRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedStockSites();
        seedSiteCategories();
        seedRejectionCategories();
    }

    private void seedAdmin() {
        if (userRepository.count() > 0) {
            return;
        }
        String password = adminPassword;
        if (password == null || password.isBlank()) {
            // No password provided — generate one and log it once so the operator can sign in.
            byte[] bytes = new byte[12];
            new SecureRandom().nextBytes(bytes);
            password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            log.warn("=====================================================================");
            log.warn(" INITIAL_ADMIN_PASSWORD was not set. Generated a temporary password:");
            log.warn("   username: {}", adminUsername);
            log.warn("   password: {}", password);
            log.warn(" Sign in and change it immediately.");
            log.warn("=====================================================================");
        }
        User admin = new User(adminEmail.trim(), adminUsername.trim(), passwordEncoder.encode(password), Role.ADMIN);
        userRepository.save(admin);
        log.info("Created initial administrator '{}'", adminUsername);
    }

    private void seedStockSites() {
        if (stockSiteRepository.count() > 0) {
            return;
        }
        record Site(String name, int order, int categoriesRequired) {}
        Site[] sites = {
                new Site("Shutterstock", 1, 1),
                new Site("Adobe Stock", 2, 1),
                new Site("iStock", 3, 0),
                new Site("Pond5", 4, 0),
                new Site("Dreamstime", 5, 1),
                new Site("123RF", 6, 0),
        };
        for (Site s : sites) {
            StockSite site = new StockSite(s.name(), Normalizer.normalize(s.name()));
            site.setDisplayOrder(s.order());
            site.setCategoriesRequired(s.categoriesRequired());
            stockSiteRepository.save(site);
        }
        log.info("Seeded {} stock sites", sites.length);
    }

    private void seedSiteCategories() {
        if (siteCategoryRepository.count() > 0) {
            return;
        }
        String[] names = {"People", "Business", "Nature", "Technology", "Lifestyle", "Healthcare"};
        for (StockSite site : stockSiteRepository.findAll()) {
            if (site.getCategoriesRequired() < 1) {
                continue;
            }
            for (String name : names) {
                siteCategoryRepository.save(new SiteCategory(site, name));
            }
        }
    }

    private void seedRejectionCategories() {
        if (rejectionCategoryRepository.count() > 0) {
            return;
        }
        String[] names = {
                "Focus", "Noise / Artifacts", "Composition", "Lighting",
                "Trademark / Property", "Similar Content", "Poor Quality", "Metadata / Keywords",
        };
        for (String name : names) {
            rejectionCategoryRepository.save(new RejectionCategory(name));
        }
    }
}
