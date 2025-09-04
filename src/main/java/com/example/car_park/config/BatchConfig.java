package com.example.car_park.config;

import com.example.car_park.controllers.dto.response.TripExportDto;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TripRepository tripRepository;

    @Bean
    public Job userProcessingJob() {
        return new JobBuilder("userProcessingJob", jobRepository)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return new StepBuilder("userProcessingStep", jobRepository)
                .<Trip, TripExportDto>chunk(100, transactionManager) // chunk size = 100
                .reader(reader(null, null))
                .processor(processor())
                .writer(writer(null))
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Trip> reader(
            @Value("#{jobParameters[begin]}")ZonedDateTime begin,
            @Value("#{jobParameters[end]}")ZonedDateTime end
    ) {
        RepositoryItemReader<Trip> reader = new RepositoryItemReader<>();
        reader.setRepository(tripRepository);
        reader.setMethodName("findAllByBeginGreaterThanEqualAndEndLessThanEqual");
        reader.setArguments(Arrays.asList(begin, end));
        reader.setPageSize(100);

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("begin", Sort.Direction.ASC);
        reader.setSort(sorts);

        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Trip, TripExportDto> processor() {
        return trip -> {
            Enterprise enterprise = trip.getVehicle().getEnterprise();
            Vehicle vehicle = trip.getVehicle();
            VehicleLocation begin = trip.getBeginLocation();
            VehicleLocation end = trip.getEndLocation();
            return new TripExportDto(
                    enterprise.getId(),
                    enterprise.getName(),
                    enterprise.getCity(),
                    enterprise.getRegistrationNumber(),
                    enterprise.getTimeZone(),
                    vehicle.getId(),
                    vehicle.getRegNum(),
                    vehicle.getPrice(),
                    vehicle.getMileage(),
                    vehicle.getProductionYear(),
                    vehicle.getColor(),
                    vehicle.isAvailable(),
                    vehicle.getPurchaseDatetime(),
                    trip.getId(),
                    trip.getBegin(),
                    trip.getEnd(),
                    begin.getId(),
                    begin.getLocation(),
                    begin.getTimestamp(),
                    end.getId(),
                    end.getLocation(),
                    end.getTimestamp()
            );
        };
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<TripExportDto> writer(
            @Value("#{jobParameters[path]}")String path
    ) {
        FlatFileItemWriter<TripExportDto> writer = new FlatFileItemWriter<>();

        // Путь к выходному файлу
        writer.setResource(new FileSystemResource(path));

        // Перезапись файла если он существует
        writer.setShouldDeleteIfExists(true);

        // Заголовок файла
        writer.setHeaderCallback(writer1 -> writer1.write("ENTERPRISE_ID,NAME,CITY,REGISTRATION_NUMBER,TIME_ZONE," +
                "VEHICLE_ID,REG_NUM,PRICE,MILEAGE,PRODUCTION_YEAR,COLOR,IS_AVAILABLE,PURCHASE_DATETIME," +
                "TRIP_ID,BEGIN,END," +
                "START_VEHICLE_LOCATION_ID,START_LOCATION,START_TIMESTAMP," +
                "END_VEHICLE_LOCATION_ID,END_LOCATION,END_TIMESTAMP"));

        // Форматирование строк
        DelimitedLineAggregator<TripExportDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        // Извлечение полей из объекта
        BeanWrapperFieldExtractor<TripExportDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
                "enterpriseId",
                "name",
                "city",
                "registrationNumber",
                "timeZone",

                "vehicleId",
                "regNum",
                "price",
                "mileage",
                "productionYear",
                "color",
                "available",
                "purchaseDatetime",

                "tripId",
                "begin",
                "end",

                "startVehicleLocationId",
                "startLocation",
                "startTimestamp",

                "endVehicleLocationId",
                "endLocation",
                "endTimestamp"
        });

        lineAggregator.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(lineAggregator);

        return writer;
    }
}
