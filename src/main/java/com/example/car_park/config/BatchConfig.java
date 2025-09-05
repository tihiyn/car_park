package com.example.car_park.config;

import com.example.car_park.controllers.dto.response.EnterpriseExportDto;
import com.example.car_park.controllers.dto.response.TripCsvExportDto;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TripRepository tripRepository;

    @Bean
    public Job csvExportJob() {
        return new JobBuilder("csvExportJob", jobRepository)
                .start(csvExportStep())
                .build();
    }

    @Bean
    public Step csvExportStep() {
        return new StepBuilder("csvExportStep", jobRepository)
                .<Trip, TripCsvExportDto>chunk(100, transactionManager) // chunk size = 100
                .reader(reader(null, null, null))
                .processor(csvProcessor())
                .writer(csvWriter(null))
                .build();
    }

    @Bean
    public Job jsonExportJob() {
        return new JobBuilder("jsonExportJob", jobRepository)
                .start(jsonExportStep())
                .build();
    }

    @Bean
    public Step jsonExportStep() {
        return new StepBuilder("jsonExportStep", jobRepository)
                .<Trip, EnterpriseExportDto>chunk(100, transactionManager) // chunk size = 100
                .reader(reader(null, null, null))
                .processor(jsonProcessor())
                .writer(jsonWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Trip> reader(
            @Value("#{jobParameters[enterpriseId]}")Long enterpriseId,
            @Value("#{jobParameters[begin]}")String beginStr,
            @Value("#{jobParameters[end]}")String endStr
    ) {
        ZonedDateTime begin = ZonedDateTime.parse(beginStr);
        ZonedDateTime end = ZonedDateTime.parse(endStr);
        ZonedDateTime beginUTC = begin.withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endUTC = end.withZoneSameInstant(ZoneId.of("UTC"));

        RepositoryItemReader<Trip> reader = new RepositoryItemReader<>();
        reader.setRepository(tripRepository);
        reader.setMethodName("findAllByVehicle_Enterprise_IdAndBeginGreaterThanEqualAndEndLessThanEqual");
        reader.setArguments(Arrays.asList(enterpriseId, beginUTC, endUTC));
        reader.setPageSize(100);

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("vehicle.id", Sort.Direction.ASC);
        sorts.put("begin", Sort.Direction.ASC);
        reader.setSort(sorts);

        return reader;
    }

    @Bean
    public ItemProcessor<Trip, TripCsvExportDto> csvProcessor() {
        return trip -> {
            Enterprise enterprise = trip.getVehicle().getEnterprise();
            ZoneId zone = enterprise.getTimeZone();
            Vehicle vehicle = trip.getVehicle();
            VehicleLocation begin = trip.getBeginLocation();
            VehicleLocation end = trip.getEndLocation();
            return new TripCsvExportDto(
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
                    vehicle.getPurchaseDatetime().withZoneSameInstant(zone),
                    trip.getId(),
                    trip.getBegin().withZoneSameInstant(zone),
                    trip.getEnd().withZoneSameInstant(zone),
                    begin.getId(),
                    begin.getLocation(),
                    begin.getTimestamp().withZoneSameInstant(zone),
                    end.getId(),
                    end.getLocation(),
                    end.getTimestamp().withZoneSameInstant(zone)
            );
        };
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<TripCsvExportDto> csvWriter(
            @Value("#{jobParameters[path]}")String path
    ) {
        FlatFileItemWriter<TripCsvExportDto> writer = new FlatFileItemWriter<>();

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
        DelimitedLineAggregator<TripCsvExportDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        // Извлечение полей из объекта
        BeanWrapperFieldExtractor<TripCsvExportDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
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

    @Bean
    public ItemProcessor<Trip, EnterpriseExportDto> jsonProcessor() {
        Map<Long, EnterpriseExportDto> cache = new HashMap<>();

        return trip -> {
            Vehicle vehicle = trip.getVehicle();
            Enterprise enterprise = vehicle.getEnterprise();
            ZoneId zone = enterprise.getTimeZone();

            // Получаем или создаём Enterprise DTO
            EnterpriseExportDto enterpriseDto = cache.computeIfAbsent(
                    enterprise.getId(),
                    id -> new EnterpriseExportDto()
                            .setId(enterprise.getId())
                            .setName(enterprise.getName())
                            .setCity(enterprise.getCity())
                            .setRegistrationNumber(enterprise.getRegistrationNumber())
                            .setTimeZone(enterprise.getTimeZone())
                            .setVehicles(new ArrayList<>())
            );

            // Ищем Vehicle DTO
            EnterpriseExportDto.VehicleExportDto vehicleDto = enterpriseDto.getVehicles().stream()
                    .filter(v -> v.getId().equals(vehicle.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        EnterpriseExportDto.VehicleExportDto newVehicle = new EnterpriseExportDto.VehicleExportDto()
                                .setId(vehicle.getId())
                                .setRegNum(vehicle.getRegNum())
                                .setPrice(vehicle.getPrice())
                                .setMileage(vehicle.getMileage())
                                .setProductionYear(vehicle.getProductionYear())
                                .setColor(vehicle.getColor())
                                .setAvailable(vehicle.isAvailable())
                                .setPurchaseDatetime(vehicle.getPurchaseDatetime().withZoneSameInstant(zone).toString())
                                .setTrips(new ArrayList<>());
                        enterpriseDto.getVehicles().add(newVehicle);
                        return newVehicle;
                    });

            // Добавляем поездку
            EnterpriseExportDto.VehicleExportDto.TripJsonExportDto tripDto = new EnterpriseExportDto.VehicleExportDto.TripJsonExportDto()
                    .setId(trip.getId())
                    .setBegin(trip.getBegin().withZoneSameInstant(zone).toString())
                    .setEnd(trip.getEnd().withZoneSameInstant(zone).toString())
                    .setBeginLocation(new EnterpriseExportDto.VehicleExportDto.TripJsonExportDto.VehicleLocationExportDto()
                            .setId(trip.getBeginLocation().getId())
                            .setLocation(trip.getBeginLocation().getLocation().toString())
                            .setTimestamp(trip.getBeginLocation().getTimestamp().withZoneSameInstant(zone).toString()))
                    .setEndLocation(new EnterpriseExportDto.VehicleExportDto.TripJsonExportDto.VehicleLocationExportDto()
                            .setId(trip.getEndLocation().getId())
                            .setLocation(trip.getEndLocation().getLocation().toString())
                            .setTimestamp(trip.getEndLocation().getTimestamp().withZoneSameInstant(zone).toString()));
            vehicleDto.getTrips().add(tripDto);

            return enterpriseDto;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<EnterpriseExportDto> jsonWriter(
            @Value("#{jobParameters[path]}")String path
    ) {
        return items -> {
            // Агрегируем дубли Enterprise
            Map<Long, EnterpriseExportDto> aggregated = new HashMap<>();
            for (EnterpriseExportDto e : items) {
                aggregated.merge(e.getId(), e, (oldE, newE) -> {
                    newE.getVehicles().forEach(vehicle -> {
                        // объединяем машины
                        EnterpriseExportDto.VehicleExportDto v = oldE.getVehicles().stream()
                                .filter(x -> x.getId().equals(vehicle.getId()))
                                .findFirst()
                                .orElse(null);
                        if (v != null) {
                            // Объединяем поездки, исключая дубликаты по ID
                            Set<Long> existingTripIds = v.getTrips().stream()
                                    .map(EnterpriseExportDto.VehicleExportDto.TripJsonExportDto::getId)
                                    .collect(Collectors.toSet());

                            vehicle.getTrips().stream()
                                    .filter(trip -> !existingTripIds.contains(trip.getId()))
                                    .forEach(trip -> v.getTrips().add(trip));
                        } else {
                            oldE.getVehicles().add(vehicle);
                        }
                    });
                    return oldE;
                });
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.writeValue(new File(path), aggregated.values());
        };
    }
}
