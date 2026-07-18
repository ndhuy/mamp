package com.microstock.export.web;

import com.microstock.export.service.ExcelExportService;
import com.microstock.media.web.dto.MediaFilter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExportController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExcelExportService exportService;

    public ExportController(ExcelExportService exportService) {
        this.exportService = exportService;
    }

    /** Exports the filtered media set (owner-scoped for users, all/owner for admins) as .xlsx. */
    @GetMapping("/api/export/media.xlsx")
    public ResponseEntity<byte[]> exportMedia(MediaFilter filter) {
        byte[] workbook = exportService.export(filter);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("media-export.xlsx")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(XLSX)
                .body(workbook);
    }
}
