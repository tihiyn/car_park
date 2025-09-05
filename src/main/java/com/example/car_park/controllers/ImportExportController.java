package com.example.car_park.controllers;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/data")
@PreAuthorize("hasRole('MANAGER')")
public class ImportExportController {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("csvExportJob")
    private Job csvJob;
    @Autowired
    @Qualifier("jsonExportJob")
    private Job jsonJob;

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/batch-exports/";

    @PostMapping("/export")
    public ResponseEntity<Resource> export(@RequestParam Long enterpriseId,
                                           @RequestParam ZonedDateTime begin,
                                           @RequestParam ZonedDateTime end,
                                           @RequestParam(defaultValue = "json", required = false) String format) {
        try {
            // Создаем временную директорию
            Files.createDirectories(Paths.get(TEMP_DIR));

            // Генерируем уникальное имя файла
            String fileName = String.format("trip_export_%d_%d.%s",
                    System.currentTimeMillis(),
                    Thread.currentThread().getId(),
                    format.equalsIgnoreCase("csv") ? "csv" : "json");
            String outputPath = TEMP_DIR + fileName;

            // Настраиваем параметры Job
            JobParametersBuilder builder = new JobParametersBuilder()
                    .addLong("enterpriseId", enterpriseId)
                    .addString("begin", begin.toString())
                    .addString("end", end.toString())
                    .addString("path", outputPath);

            // Запускаем Job синхронно
            JobExecution execution = format.equalsIgnoreCase("csv")
                    ? jobLauncher.run(csvJob, builder.toJobParameters())
                    : jobLauncher.run(jsonJob, builder.toJobParameters());

            // Проверяем результат выполнения
            if (execution.getStatus() != BatchStatus.COMPLETED) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }

            // Проверяем существование файла
            File file = new File(outputPath);
            if (!file.exists() || file.length() == 0) {
                return ResponseEntity.notFound().build();
            }

            // Подготавливаем файл для скачивания
            Resource resource = new InputStreamResource(new FileInputStream(file)) {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(file) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            // Удаляем файл после закрытия потока
                            scheduleDeletion(file);
                        }
                    };
                }
            };

            // Формируем имя файла для скачивания
            String downloadFileName = String.format("trips_%s.%s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")),
                    format.equalsIgnoreCase("csv") ? "csv" : "json");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, String.format("text/%s; charset=UTF-8", format.equalsIgnoreCase("csv") ? "csv" : "json"))
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Метод для планирования удаления файла
    private void scheduleDeletion(File file) {
        // Планируем удаление через 5 минут
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                if (file.exists()) {
                    Files.delete(file.toPath());
                    System.out.println("Deleted temporary file: " + file.getName());
                }
            } catch (IOException e) {
                System.err.println("Failed to delete temporary file: " + file.getName());
            } finally {
                executor.shutdown();
            }
        }, 5, TimeUnit.MINUTES);
    }
}
