package com.microstock.export.service;

import com.microstock.common.security.OwnershipGuard;
import com.microstock.common.security.SecurityUtils;
import com.microstock.concept.domain.Concept;
import com.microstock.keyword.domain.Keyword;
import com.microstock.masterdata.domain.CaptureDevice;
import com.microstock.masterdata.domain.Lens;
import com.microstock.media.domain.MediaAsset;
import com.microstock.media.repository.MediaAssetRepository;
import com.microstock.media.repository.MediaSpecifications;
import com.microstock.media.web.dto.MediaFilter;
import com.microstock.submission.domain.SubmissionRecord;
import com.microstock.submission.repository.SubmissionRecordRepository;
import com.microstock.user.domain.User;
import com.microstock.user.repository.UserRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Builds the multi-worksheet Excel export (PRD 9). Respects filters and ownership scope. */
@Service
public class ExcelExportService {

    private static final int MAX_ROWS = 10_000; // large exports would move to a background job

    private final MediaAssetRepository mediaRepository;
    private final SubmissionRecordRepository submissionRepository;
    private final UserRepository userRepository;
    private final OwnershipGuard ownershipGuard;

    public ExcelExportService(
            MediaAssetRepository mediaRepository,
            SubmissionRecordRepository submissionRepository,
            UserRepository userRepository,
            OwnershipGuard ownershipGuard) {
        this.mediaRepository = mediaRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.ownershipGuard = ownershipGuard;
    }

    @Transactional(readOnly = true)
    public byte[] export(MediaFilter filter) {
        var spec = MediaSpecifications.build(filter, SecurityUtils.currentUserId(), ownershipGuard.isAdmin());
        List<MediaAsset> media = mediaRepository
                .findAll(spec, PageRequest.of(0, MAX_ROWS, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();

        List<UUID> mediaIds = media.stream().map(MediaAsset::getId).toList();
        Map<UUID, List<SubmissionRecord>> submissionsByMedia = mediaIds.isEmpty()
                ? Map.of()
                : submissionRepository.findByMediaIdIn(mediaIds).stream()
                        .collect(Collectors.groupingBy(s -> s.getMedia().getId()));
        Map<UUID, String> owners = userRepository
                .findAllById(media.stream().map(MediaAsset::getOwnerId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, User::getUsername));

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle header = headerStyle(wb);
            writeMediaSheet(wb, header, media, owners);
            writeSubmissionsSheet(wb, header, media, submissionsByMedia);
            writeSummarySheet(wb, header, media, submissionsByMedia);
            writeKeywordsSheet(wb, header, media);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build Excel export", e);
        }
    }

    // ----------------------------------------------------------------------

    private void writeMediaSheet(Workbook wb, CellStyle header, List<MediaAsset> media, Map<UUID, String> owners) {
        Sheet sheet = wb.createSheet("Media");
        String[] cols = {"Media ID", "Owner", "Title", "Type", "Workflow Status", "Content Usage",
                "Capture Device", "Lens", "Capture Date", "Location", "Concepts", "Keywords",
                "Thumbnail Key", "Original File Path", "Export File Path", "Storage Type",
                "AI Generated", "Deleted", "Created", "Updated"};
        writeHeader(sheet, header, cols);
        int r = 1;
        for (MediaAsset m : media) {
            Row row = sheet.createRow(r++);
            int c = 0;
            set(row, c++, m.getCode());
            set(row, c++, owners.getOrDefault(m.getOwnerId(), ""));
            set(row, c++, m.getTitle());
            set(row, c++, m.getMediaType().name());
            set(row, c++, m.getWorkflowStatus().name());
            set(row, c++, m.getContentUsageType() == null ? "" : m.getContentUsageType().name());
            set(row, c++, deviceName(m.getCaptureDevice()));
            set(row, c++, lensName(m.getLens()));
            set(row, c++, str(m.getCaptureDate()));
            set(row, c++, m.getLocation());
            set(row, c++, m.getConcepts().stream().map(Concept::getName).collect(Collectors.joining(", ")));
            set(row, c++, m.getKeywords().stream().map(Keyword::getValue).collect(Collectors.joining(", ")));
            set(row, c++, m.getThumbnailKey());
            set(row, c++, m.getOriginalFilePath());
            set(row, c++, m.getExportFilePath());
            set(row, c++, m.getStorageType() == null ? "" : m.getStorageType().name());
            set(row, c++, m.isAiGenerated() ? "Yes" : "No");
            set(row, c++, m.isDeleted() ? "Yes" : "No");
            set(row, c++, str(toDate(m.getCreatedAt())));
            set(row, c, str(toDate(m.getUpdatedAt())));
        }
        autosize(sheet, cols.length);
    }

    private void writeSubmissionsSheet(
            Workbook wb, CellStyle header, List<MediaAsset> media, Map<UUID, List<SubmissionRecord>> byMedia) {
        Sheet sheet = wb.createSheet("Submission Records");
        String[] cols = {"Media ID", "Title", "Stock Site", "Status", "Primary Category", "Secondary Category",
                "Contributor Asset ID", "Asset URL", "Submitted Date", "Reviewed Date",
                "Rejection Category", "Rejection Detail", "Notes"};
        writeHeader(sheet, header, cols);
        int r = 1;
        for (MediaAsset m : media) {
            List<SubmissionRecord> subs = byMedia.getOrDefault(m.getId(), List.of()).stream()
                    .sorted(Comparator.comparing(s -> s.getStockSite().getName()))
                    .toList();
            for (SubmissionRecord s : subs) {
                Row row = sheet.createRow(r++);
                int c = 0;
                set(row, c++, m.getCode());
                set(row, c++, m.getTitle());
                set(row, c++, s.getStockSite().getName());
                set(row, c++, s.getStatus().name());
                set(row, c++, s.getPrimaryCategory() == null ? "" : s.getPrimaryCategory().getName());
                set(row, c++, s.getSecondaryCategory() == null ? "" : s.getSecondaryCategory().getName());
                set(row, c++, s.getContributorAssetId());
                set(row, c++, s.getAssetUrl());
                set(row, c++, str(s.getSubmittedDate()));
                set(row, c++, str(s.getReviewedDate()));
                set(row, c++, s.getRejectionCategory() == null ? "" : s.getRejectionCategory().getName());
                set(row, c++, s.getRejectionDetail());
                set(row, c, s.getNotes());
            }
        }
        autosize(sheet, cols.length);
    }

    private void writeSummarySheet(
            Workbook wb, CellStyle header, List<MediaAsset> media, Map<UUID, List<SubmissionRecord>> byMedia) {
        Sheet sheet = wb.createSheet("Summary");
        long photos = media.stream().filter(m -> m.getMediaType().name().equals("PHOTO")).count();
        long footage = media.size() - photos;
        long ready = media.stream().filter(m -> m.getWorkflowStatus().name().equals("READY")).count();

        List<SubmissionRecord> allSubs = byMedia.values().stream().flatMap(List::stream).toList();
        Map<String, Long> byStatus = allSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getStatus().name(), TreeMap::new, Collectors.counting()));
        Map<String, Long> bySite = allSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getStockSite().getName(), TreeMap::new, Collectors.counting()));

        int r = 0;
        r = section(sheet, header, r, "Totals");
        r = kv(sheet, r, "Total media", media.size());
        r = kv(sheet, r, "Photos", photos);
        r = kv(sheet, r, "Footage", footage);
        r = kv(sheet, r, "Ready for Upload", ready);
        r = kv(sheet, r, "Total submissions", allSubs.size());
        r++;
        r = section(sheet, header, r, "Submissions by status");
        for (var e : byStatus.entrySet()) r = kv(sheet, r, e.getKey(), e.getValue());
        r++;
        r = section(sheet, header, r, "Submissions by stock site");
        for (var e : bySite.entrySet()) r = kv(sheet, r, e.getKey(), e.getValue());

        autosize(sheet, 2);
    }

    private void writeKeywordsSheet(Workbook wb, CellStyle header, List<MediaAsset> media) {
        Sheet sheet = wb.createSheet("Keywords");
        writeHeader(sheet, header, new String[]{"Media ID", "Title", "Keyword"});
        int r = 1;
        for (MediaAsset m : media) {
            for (Keyword k : m.getKeywords()) {
                Row row = sheet.createRow(r++);
                set(row, 0, m.getCode());
                set(row, 1, m.getTitle());
                set(row, 2, k.getValue());
            }
        }
        autosize(sheet, 3);
    }

    // ----------------------------------------------------------------------

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void writeHeader(Sheet sheet, CellStyle style, String[] cols) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(style);
        }
    }

    private int section(Sheet sheet, CellStyle style, int r, String title) {
        Cell cell = sheet.createRow(r).createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        return r + 1;
    }

    private int kv(Sheet sheet, int r, String key, long value) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
        return r + 1;
    }

    private void set(Row row, int col, String value) {
        row.createCell(col).setCellValue(value == null ? "" : value);
    }

    private void autosize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private String deviceName(CaptureDevice d) {
        return d == null ? "" : d.getBrand() + " " + d.getModel();
    }

    private String lensName(Lens l) {
        return l == null ? "" : l.getBrand() + " " + l.getModel();
    }

    private String str(LocalDate d) {
        return d == null ? "" : d.toString();
    }

    private LocalDate toDate(java.time.Instant instant) {
        return instant == null ? null : instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
