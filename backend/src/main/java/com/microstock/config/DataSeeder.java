package com.microstock.config;

import com.microstock.common.domain.ContentUsageType;
import com.microstock.common.domain.DeviceType;
import com.microstock.common.domain.MediaType;
import com.microstock.common.domain.Role;
import com.microstock.common.domain.WorkflowStatus;
import com.microstock.common.util.Normalizer;
import com.microstock.masterdata.domain.CaptureDevice;
import com.microstock.masterdata.domain.RejectionCategory;
import com.microstock.masterdata.domain.SiteCategory;
import com.microstock.masterdata.domain.StockSite;
import com.microstock.masterdata.repository.CaptureDeviceRepository;
import com.microstock.masterdata.repository.RejectionCategoryRepository;
import com.microstock.masterdata.repository.SiteCategoryRepository;
import com.microstock.masterdata.repository.StockSiteRepository;
import com.microstock.media.domain.MediaAsset;
import com.microstock.media.repository.MediaAssetRepository;
import com.microstock.user.domain.User;
import com.microstock.user.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default administrator and baseline global master data on first run so
 * the platform is usable immediately in development. Disabled in the prod profile.
 */
@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockSiteRepository stockSiteRepository;
    private final SiteCategoryRepository siteCategoryRepository;
    private final CaptureDeviceRepository captureDeviceRepository;
    private final RejectionCategoryRepository rejectionCategoryRepository;
    private final MediaAssetRepository mediaAssetRepository;

    public DataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StockSiteRepository stockSiteRepository,
            SiteCategoryRepository siteCategoryRepository,
            CaptureDeviceRepository captureDeviceRepository,
            RejectionCategoryRepository rejectionCategoryRepository,
            MediaAssetRepository mediaAssetRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.stockSiteRepository = stockSiteRepository;
        this.siteCategoryRepository = siteCategoryRepository;
        this.captureDeviceRepository = captureDeviceRepository;
        this.rejectionCategoryRepository = rejectionCategoryRepository;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedStockSites();
        seedSiteCategories();
        seedCaptureDevices();
        seedRejectionCategories();
        seedSampleMedia();
    }

    private void seedAdmin() {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User(
                "admin@microstock.local", "admin", passwordEncoder.encode("Admin123!"), Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded default administrator: admin@microstock.local / Admin123!");
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

    /** Seed a shared category vocabulary for sites that require categories. */
    private void seedSiteCategories() {
        if (siteCategoryRepository.count() > 0) {
            return;
        }
        String[] names = {"People", "Business", "Nature", "Technology", "Lifestyle", "Healthcare"};
        int seeded = 0;
        for (StockSite site : stockSiteRepository.findAll()) {
            if (site.getCategoriesRequired() < 1) {
                continue;
            }
            for (String name : names) {
                siteCategoryRepository.save(new SiteCategory(site, name));
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("Seeded {} site categories", seeded);
        }
    }

    private void seedCaptureDevices() {
        if (captureDeviceRepository.count() > 0) {
            return;
        }
        record Dev(String brand, String model, DeviceType type) {}
        Dev[] devices = {
                new Dev("Fujifilm", "X-H2S", DeviceType.INTERCHANGEABLE_LENS),
                new Dev("Fujifilm", "GFX100S", DeviceType.INTERCHANGEABLE_LENS),
                new Dev("Apple", "iPhone 15 Pro Max", DeviceType.SMARTPHONE),
                new Dev("DJI", "Mini 4 Pro", DeviceType.DRONE),
                new Dev("GoPro", "HERO12", DeviceType.ACTION_CAMERA),
        };
        for (Dev d : devices) {
            captureDeviceRepository.save(new CaptureDevice(
                    d.brand(), d.model(), Normalizer.key(d.brand(), d.model()), d.type()));
        }
        log.info("Seeded {} capture devices", devices.length);
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
        log.info("Seeded {} rejection categories", names.length);
    }

    /** A handful of sample media for the admin so lists/dashboard show data on first run. */
    private void seedSampleMedia() {
        if (mediaAssetRepository.count() > 0) {
            return;
        }
        User admin = userRepository.findByUsernameIgnoreCase("admin").orElse(null);
        List<CaptureDevice> devices = captureDeviceRepository.findAll();
        if (admin == null || devices.isEmpty()) {
            return;
        }
        CaptureDevice device = devices.get(0);
        record Sample(String title, MediaType type, WorkflowStatus status) {}
        Sample[] samples = {
                new Sample("Doctor with patient", MediaType.PHOTO, WorkflowStatus.READY),
                new Sample("Happy family at home", MediaType.PHOTO, WorkflowStatus.COMPLETED),
                new Sample("Mountain landscape", MediaType.PHOTO, WorkflowStatus.METADATA_PENDING),
                new Sample("City skyline sunset", MediaType.PHOTO, WorkflowStatus.DRAFT),
                new Sample("Business meeting", MediaType.PHOTO, WorkflowStatus.READY),
                new Sample("Coffee and laptop", MediaType.FOOTAGE, WorkflowStatus.EDITING),
        };
        for (Sample s : samples) {
            MediaAsset media = new MediaAsset(admin.getId(), s.title(), s.type());
            media.setCaptureDevice(device);
            media.setWorkflowStatus(s.status());
            media.setContentUsageType(ContentUsageType.COMMERCIAL);
            mediaAssetRepository.save(media);
        }
        log.info("Seeded {} sample media assets", samples.length);
    }
}
