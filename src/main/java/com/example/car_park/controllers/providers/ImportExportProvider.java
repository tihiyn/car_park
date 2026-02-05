package com.example.car_park.controllers.providers;

import com.example.car_park.controllers.dto.response.ExportResp;
import com.example.car_park.controllers.dto.response.ImportResp;
import com.example.car_park.service.ImportExportService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ImportExportProvider {
    private final ImportExportService ies;
    private final JobLauncher jobLauncher;
    private final Job csvJob;
    private final Job jsonJob;
    private final Job jsonImportJob;
    private final Job csvImportJob;

    public ImportExportProvider(ImportExportService ies,
                                JobLauncher jobLauncher,
                                @Qualifier("csvExportJob") Job csvJob,
                                @Qualifier("jsonExportJob") Job jsonJob,
                                @Qualifier("jsonImportJob") Job jsonImportJob,
                                @Qualifier("csvImportJob")Job csvImportJob) {
        this.ies = ies;
        this.jobLauncher = jobLauncher;
        this.csvJob = csvJob;
        this.jsonJob = jsonJob;
        this.jsonImportJob = jsonImportJob;
        this.csvImportJob = csvImportJob;
    }

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir") + "/batch-exports/";

    public ExportResp exportData(Long eId, ZonedDateTime s, ZonedDateTime b, String format) {
        try {
            Files.createDirectories(Paths.get(TMP_DIR));
            String outputPath = "%s%s".formatted(TMP_DIR, ies.generateUniqueFileName(format));
            JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("enterpriseId", eId)
                .addString("begin", s.toString())
                .addString("end", b.toString())
                .addString("path", outputPath);
            JobExecution ex = format.equalsIgnoreCase("csv")
                ? jobLauncher.run(csvJob, builder.toJobParameters())
                : jobLauncher.run(jsonJob, builder.toJobParameters());
            if (ex.getStatus() != BatchStatus.COMPLETED) {
                return null;
            }
            File f = new File(outputPath);
            Resource res = new InputStreamResource(new FileInputStream(f)) {
                @Override
                public InputStream getInputStream() throws FileNotFoundException {
                    return new FileInputStream(f) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            deleteOnSchedule(f);
                        }
                    };
                }
            };
            return ies.buildExportResp(res, f.length(), format);
        } catch (IOException e) {
            System.err.println("Ошибка при создании файла");
            return null;
        } catch (JobExecutionException e) {
            System.err.println("Ошибка при выполнении экспорта");
            return null;
        }
    }

    public ImportResp importData(String format, MultipartFile file) {
        if (file.isEmpty()) {
            return new ImportResp(false, "Файл пуст");
        }
        try {
            File tmp = File.createTempFile("import-", ".csv");
            file.transferTo(tmp);
            JobParametersBuilder jobParameters = new JobParametersBuilder()
                .addString("path", tmp.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis());
            JobExecution ex = jobLauncher.run(csvImportJob, jobParameters.toJobParameters());
            if (ex.getStatus() != BatchStatus.COMPLETED) {
                return new ImportResp(false, "Ошибка при выполнении импорта");
            }
            return new ImportResp(true, "Импорт завершён успешно");
        } catch (IOException e) {
            return new ImportResp(false, "Не удалось создать файл");
        } catch (JobExecutionException e) {
            return new ImportResp(false, "Ошибка при выполнении импорта");
        }
    }

    private void deleteOnSchedule(File f) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                if (f.exists()) {
                    Files.delete(f.toPath());
                }
            } catch (IOException e) {
                System.err.printf("Ошибка при удалении файла: %s", f.getName());
            } finally {
                executor.shutdown();
            }
        }, 5, TimeUnit.MINUTES);
    }
}
