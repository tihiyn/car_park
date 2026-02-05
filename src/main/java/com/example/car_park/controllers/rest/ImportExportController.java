package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.response.ExportResp;
import com.example.car_park.controllers.dto.response.ImportResp;
import com.example.car_park.controllers.providers.ImportExportProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/data")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class ImportExportController {
    private final ImportExportProvider p;

    @PostMapping("/export")
    public ResponseEntity<Resource> exportData(@RequestParam Long enterpriseId,
                                               @RequestParam ZonedDateTime begin,
                                               @RequestParam ZonedDateTime end,
                                               @RequestParam(defaultValue = "json", required = false) String format) {
        ExportResp resp = p.exportData(enterpriseId, begin, end, format);
        if (resp == null) {
            return ResponseEntity.internalServerError().body(null);
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(resp.getFileName()))
            .header(HttpHeaders.CONTENT_TYPE, "text/%s; charset=UTF-8".formatted(format.equalsIgnoreCase("csv") ? "csv" : "json"))
            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resp.getFileLength()))
            .body(resp.getRes());
    }

    @PostMapping("/import")
    public ResponseEntity<String> importData(@RequestParam(defaultValue = "json", required = false) String format,
                                             @RequestParam("file") MultipartFile file) {
        ImportResp resp = p.importData(format, file);
        if (resp.isSuccessful()) {
            return ResponseEntity.ok(resp.getDesc());
        }
        return ResponseEntity.internalServerError().body(resp.getDesc());
    }
}
