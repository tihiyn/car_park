package com.example.car_park.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequiredArgsConstructor
public class ImportExportController {
    private final JobLauncher jobLauncher;
    private final Job job;

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/batch-exports/";

    public ResponseEntity<Resource> export(@RequestParam ZonedDateTime begin,
                                           @RequestParam ZonedDateTime end) {
        try {
            // Создаем временную директорию
            Files.createDirectories(Paths.get(TEMP_DIR));

            // Генерируем уникальное имя файла
            String fileName = String.format("trip_export_%d_%d.csv",
                    System.currentTimeMillis(),
                    Thread.currentThread().getId());
            String outputPath = TEMP_DIR + fileName;

            // Настраиваем параметры Job
            JobParametersBuilder builder = new JobParametersBuilder()
                    .addJobParameter("begin", begin, ZonedDateTime.class)
                    .addJobParameter("end", end, ZonedDateTime.class)
                    .addString("path", outputPath);

            // Запускаем Job синхронно
            JobExecution execution = jobLauncher.run(job, builder.toJobParameters());

            // Проверяем результат выполнения
            if (execution.getStatus() != BatchStatus.COMPLETED) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }

            // Проверяем существование файла
            File csvFile = new File(outputPath);
            if (!csvFile.exists() || csvFile.length() == 0) {
                return ResponseEntity.notFound().build();
            }

            // Подготавливаем файл для скачивания
            Resource resource = new InputStreamResource(new FileInputStream(csvFile)) {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(csvFile) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            // Удаляем файл после закрытия потока
                            scheduleDeletion(csvFile);
                        }
                    };
                }
            };

            // Формируем имя файла для скачивания
            String downloadFileName = String.format("trips_%s.csv",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(csvFile.length()))
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
