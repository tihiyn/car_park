package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.ExportResp;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ImportExportService {
    public String generateUniqueFileName(String format) {
        return "trip_export_%d_%d.%s".formatted(
            System.currentTimeMillis(),
            Thread.currentThread().threadId(),
            format.equalsIgnoreCase("csv") ? "csv" : "json"
        );
    }

    public ExportResp buildExportResp(Resource res, Long length, String format) {
        return new ExportResp()
            .setRes(res)
            .setFileName("trips_%s.%s".formatted(
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")),
                format.equalsIgnoreCase("csv") ? "csv" : "json")
            )
            .setFileLength(length);
    }
}
