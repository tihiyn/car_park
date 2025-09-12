package com.example.car_park.config;

import com.example.car_park.controllers.dto.request.TripCsvImportDto;
import com.example.car_park.controllers.dto.response.EnterpriseExportDto;
import com.example.car_park.controllers.dto.response.TripCsvExportDto;
import com.example.car_park.dao.EnterpriseRepository;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.VehicleLocationRepository;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TripRepository tripRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleLocationRepository vehicleLocationRepository;

    private static final Map<String, UUID> ID_CACHE = new ConcurrentHashMap<>();

    @Bean
    public Job csvExportJob() {
        return new JobBuilder("csvExportJob", jobRepository)
                .start(csvExportStep())
                .build();
    }

    @Bean
    public Step csvExportStep() {
        return new StepBuilder("csvExportStep", jobRepository)
                .<Trip, TripCsvExportDto>chunk(100, transactionManager)
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
                    ID_CACHE.computeIfAbsent("enterprise" + enterprise.getId(), id -> UUID.randomUUID()),
                    enterprise.getName(),
                    enterprise.getCity(),
                    enterprise.getRegistrationNumber(),
                    enterprise.getTimeZone(),
                    ID_CACHE.computeIfAbsent("vehicle" + enterprise.getId(), id -> UUID.randomUUID()),
                    vehicle.getRegNum(),
                    vehicle.getPrice(),
                    vehicle.getMileage(),
                    vehicle.getProductionYear(),
                    vehicle.getColor(),
                    vehicle.isAvailable(),
                    vehicle.getPurchaseDatetime().withZoneSameInstant(zone),
                    ID_CACHE.computeIfAbsent("trip" + enterprise.getId(), id -> UUID.randomUUID()),
                    trip.getBegin().withZoneSameInstant(zone),
                    trip.getEnd().withZoneSameInstant(zone),
                    ID_CACHE.computeIfAbsent("vehicleLocation" + enterprise.getId(), id -> UUID.randomUUID()),
                    begin.getLocation(),
                    begin.getTimestamp().withZoneSameInstant(zone),
                    ID_CACHE.computeIfAbsent("vehicleLocation" + enterprise.getId(), id -> UUID.randomUUID()),
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
        Map<Long, EnterpriseExportDto> enterpriseCache = new HashMap<>();
        Map<Long, Set<Long>> enterpriseVehicleIds = new HashMap<>(); // id автомобилей для каждого предприятия

        return trip -> {
            Vehicle vehicle = trip.getVehicle();
            Enterprise enterprise = vehicle.getEnterprise();
            ZoneId zone = enterprise.getTimeZone();

            // Получаем или создаём Enterprise DTO
            EnterpriseExportDto enterpriseDto = enterpriseCache.computeIfAbsent(
                    enterprise.getId(),
                    id -> new EnterpriseExportDto()
                            .setId(ID_CACHE.computeIfAbsent("enterprise" + enterprise.getId(), eId -> UUID.randomUUID()))
                            .setName(enterprise.getName())
                            .setCity(enterprise.getCity())
                            .setRegistrationNumber(enterprise.getRegistrationNumber())
                            .setTimeZone(zone)
                            .setVehicles(new ArrayList<>())
            );

            // Инициализируем Set для автомобилей предприятия
            enterpriseVehicleIds.computeIfAbsent(enterprise.getId(), id -> new HashSet<>());

            EnterpriseExportDto.VehicleExportDto vehicleDto;

            // Если автомобиль ещё не добавлен, создаём его
            if (!enterpriseVehicleIds.get(enterprise.getId()).contains(vehicle.getId())) {
                vehicleDto = new EnterpriseExportDto.VehicleExportDto()
                        .setId(ID_CACHE.computeIfAbsent("vehicle" + vehicle.getId(), vId -> UUID.randomUUID()))
                        .setRegNum(vehicle.getRegNum())
                        .setPrice(vehicle.getPrice())
                        .setMileage(vehicle.getMileage())
                        .setProductionYear(vehicle.getProductionYear())
                        .setColor(vehicle.getColor())
                        .setAvailable(vehicle.isAvailable())
                        .setPurchaseDatetime(vehicle.getPurchaseDatetime().withZoneSameInstant(zone).toString())
                        .setTrips(new ArrayList<>());
                enterpriseDto.getVehicles().add(vehicleDto);
                enterpriseVehicleIds.get(enterprise.getId()).add(vehicle.getId());
            } else {
                // Иначе находим существующий Vehicle DTO
                vehicleDto = enterpriseDto.getVehicles().stream()
                        .filter(v -> v.getId().equals(ID_CACHE.get("vehicle" + vehicle.getId())))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Vehicle должен существовать"));
            }

            // Добавляем поездку
            EnterpriseExportDto.VehicleExportDto.TripJsonExportDto tripDto = new EnterpriseExportDto.VehicleExportDto.TripJsonExportDto()
                    .setId(ID_CACHE.computeIfAbsent("trip" + trip.getId(), id -> UUID.randomUUID()))
                    .setBegin(trip.getBegin().withZoneSameInstant(zone).toString())
                    .setEnd(trip.getEnd().withZoneSameInstant(zone).toString())
                    .setBeginLocation(new EnterpriseExportDto.VehicleExportDto.TripJsonExportDto.VehicleLocationExportDto()
                            .setId(ID_CACHE.computeIfAbsent("vehicleLocation" + trip.getBeginLocation().getId(), id -> UUID.randomUUID()))
                            .setLocation(trip.getBeginLocation().getLocation().toString())
                            .setTimestamp(trip.getBeginLocation().getTimestamp().withZoneSameInstant(zone).toString()))
                    .setEndLocation(new EnterpriseExportDto.VehicleExportDto.TripJsonExportDto.VehicleLocationExportDto()
                            .setId(ID_CACHE.computeIfAbsent("vehicleLocation" + trip.getEndLocation().getId(), id -> UUID.randomUUID()))
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
            Map<UUID, EnterpriseExportDto> aggregated = new ConcurrentHashMap<>();

            for (EnterpriseExportDto e : items) {
                aggregated.merge(e.getId(), e, (oldE, newE) -> {
                    for (EnterpriseExportDto.VehicleExportDto vehicle : newE.getVehicles()) {
                        EnterpriseExportDto.VehicleExportDto existingVehicle = oldE.getVehicles().stream()
                                .filter(x -> x.getId().equals(vehicle.getId()))
                                .findFirst()
                                .orElse(null);

                        if (existingVehicle != null) {
                            // Объединяем поездки без дубликатов
                            Set<UUID> existingTripIds = existingVehicle.getTrips().stream()
                                    .map(EnterpriseExportDto.VehicleExportDto.TripJsonExportDto::getId)
                                    .collect(Collectors.toSet());

                            vehicle.getTrips().stream()
                                    .filter(trip -> !existingTripIds.contains(trip.getId()))
                                    .forEach(trip -> existingVehicle.getTrips().add(trip));
                        } else {
                            // Добавляем новую машину
                            oldE.getVehicles().add(vehicle);
                        }
                    }
                    return oldE;
                });
            }


            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.writeValue(new File(path), aggregated.values());
        };
    }

    @Bean
    public Job jsonImportJob() {
        return new JobBuilder("jsonImportJob", jobRepository)
                .start(jsonImportStep())
                .build();
    }

    @Bean
    public Step jsonImportStep() {
        return new StepBuilder("jsonImportStep", jobRepository)
                .<EnterpriseExportDto, Enterprise>chunk(10, transactionManager)
                .reader(jsonImportReader(null))
                .processor(jsonImportProcessor())
                .writer(jsonImportWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<EnterpriseExportDto> jsonImportReader(
            @Value("#{jobParameters[path]}") String path
    ) {
        return new ItemReader<>() {
            private Iterator<EnterpriseExportDto> iterator;

            @Override
            public EnterpriseExportDto read() throws Exception {
                if (iterator == null) {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.findAndRegisterModules();
                    List<EnterpriseExportDto> enterprises = mapper.readValue(
                            new File(path),
                            new TypeReference<>() {
                            }
                    );
                    iterator = enterprises.iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    public ItemProcessor<EnterpriseExportDto, Enterprise> jsonImportProcessor() {
        return enterpriseDto -> {
            System.out.println("Start processing enterprise ID: " + enterpriseDto.getId());
            // Проверяем наличие Enterprise
            Enterprise enterprise = new Enterprise()
                        .setName(enterpriseDto.getName())
                        .setCity(enterpriseDto.getCity())
                        .setRegistrationNumber(enterpriseDto.getRegistrationNumber())
                        .setTimeZone(enterpriseDto.getTimeZone());

            // Сопоставляем машины
            for (EnterpriseExportDto.VehicleExportDto vehicleDto : enterpriseDto.getVehicles()) {
                Vehicle vehicle = new Vehicle()
                            .setEnterprise(enterprise);
                vehicle.setRegNum(vehicleDto.getRegNum());
                vehicle.setPrice(vehicleDto.getPrice());
                vehicle.setMileage(vehicleDto.getMileage());
                vehicle.setProductionYear(vehicleDto.getProductionYear());
                vehicle.setColor(vehicleDto.getColor());
                vehicle.setAvailable(vehicleDto.isAvailable());
                vehicle.setPurchaseDatetime(ZonedDateTime.parse(vehicleDto.getPurchaseDatetime()));

                // Добавляем поездки
                for (EnterpriseExportDto.VehicleExportDto.TripJsonExportDto tripDto : vehicleDto.getTrips()) {
                    Trip trip = new Trip()
                                .setVehicle(vehicle);

                    trip.setBegin(ZonedDateTime.parse(tripDto.getBegin()));
                    trip.setEnd(ZonedDateTime.parse(tripDto.getEnd()));

                    trip.setBeginLocation(new VehicleLocation()
                            .setVehicle(vehicle)
                            .setLocation(parsePointFromString(tripDto.getBeginLocation().getLocation()))
                            .setTimestamp(ZonedDateTime.parse(tripDto.getBeginLocation().getTimestamp()))
                    );

                    trip.setEndLocation(new VehicleLocation()
                            .setVehicle(vehicle)
                            .setLocation(parsePointFromString(tripDto.getEndLocation().getLocation()))
                            .setTimestamp(ZonedDateTime.parse(tripDto.getEndLocation().getTimestamp()))
                    );

                    vehicle.getTrips().add(trip);
                }

                enterprise.getVehicles().add(vehicle);
            }

            return enterprise;
        };
    }

    @Bean
    public ItemWriter<Enterprise> jsonImportWriter() {
        return items -> {
            System.out.println("Start writing " + items.size() + " enterprises to DB");
            for (Enterprise e : items) {
                enterpriseRepository.save(e);
            }
        };
    }

    private Point parsePointFromString(String wktPoint) {
        // Удаляем "POINT" и скобки, оставляем только числа
        String cleaned = wktPoint.replace("POINT", "")
                .replace("(", "")
                .replace(")", "")
                .trim();

        // Разделяем координаты по пробелу
        String[] coords = cleaned.split(" ");

        if (coords.length < 2) {
            throw new IllegalArgumentException("Invalid POINT format: " + wktPoint);
        }

        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);

        return new GeometryFactory(new PrecisionModel(), 4326).createPoint(new Coordinate(x, y));
    }

    @Bean
    public Job csvImportJob() {
        return new JobBuilder("csvImportJob", jobRepository)
                .start(csvImportStep())
                .build();
    }

    @Bean
    public Step csvImportStep() {
        return new StepBuilder("csvImportStep", jobRepository)
                .<TripCsvImportDto, Trip>chunk(100, transactionManager)
                .reader(csvImportReader(null))
                .processor(csvImportProcessor())
                .writer(tripWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<TripCsvImportDto> csvImportReader(
            @Value("#{jobParameters[path]}") String path
    ) {
        FlatFileItemReader<TripCsvImportDto> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(path));

        // Пропускаем заголовок
        reader.setLinesToSkip(1);

        DefaultLineMapper<TripCsvImportDto> lineMapper = new DefaultLineMapper<>();

        // Разделитель — запятая
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(
                "enterpriseId","name","city","registrationNumber","timeZone",
                "vehicleId","regNum","price","mileage","productionYear","color","available","purchaseDatetime",
                "tripId","begin","end",
                "startVehicleLocationId","startLocation","startTimestamp",
                "endVehicleLocationId","endLocation","endTimestamp"
        );

        BeanWrapperFieldSetMapper<TripCsvImportDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(TripCsvImportDto.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    public ItemProcessor<TripCsvImportDto, Trip> csvImportProcessor() {
        return dto -> {
            System.out.println("Start processing enterprise ID: " + dto.getEnterpriseId());
            // 1. Предприятие
            Enterprise enterprise = enterpriseRepository.findByRegistrationNumber(dto.getRegistrationNumber())
                    .orElseGet(() -> {
                        Enterprise e = new Enterprise();
                        e.setName(dto.getName());
                        e.setCity(dto.getCity());
                        e.setRegistrationNumber(dto.getRegistrationNumber());
                        e.setTimeZone(dto.getTimeZone());
                        return enterpriseRepository.save(e);
                    });

            // 2. Машина
            Vehicle vehicle = vehicleRepository.findByRegNum(dto.getRegNum())
                    .orElseGet(() -> {
                        Vehicle v = new Vehicle();
                        v.setEnterprise(enterprise);
                        v.setRegNum(dto.getRegNum());
                        v.setPrice(dto.getPrice());
                        v.setMileage(dto.getMileage());
                        v.setProductionYear(dto.getProductionYear());
                        v.setColor(dto.getColor());
                        v.setAvailable(dto.isAvailable());
                        v.setPurchaseDatetime(ZonedDateTime.parse(dto.getPurchaseDatetime()).withZoneSameInstant(ZoneId.of("UTC")));
                        return vehicleRepository.save(v);
                    });

            enterprise.getVehicles().add(vehicle);


            // 3. Локации
            VehicleLocation start = vehicleLocationRepository.findById(dto.getStartVehicleLocationId())
                    .orElseGet(() -> {
                        VehicleLocation loc = new VehicleLocation();
                        loc.setVehicle(vehicle);
                        loc.setLocation(parsePointFromString(dto.getStartLocation()));
                        loc.setTimestamp(ZonedDateTime.parse(dto.getStartTimestamp()).withZoneSameInstant(ZoneId.of("UTC")));
                        return loc;
                    });

            VehicleLocation end = vehicleLocationRepository.findById(dto.getEndVehicleLocationId())
                    .orElseGet(() -> {
                        VehicleLocation loc = new VehicleLocation();
                        loc.setVehicle(vehicle);
                        loc.setLocation(parsePointFromString(dto.getEndLocation()));
                        loc.setTimestamp(ZonedDateTime.parse(dto.getEndTimestamp()).withZoneSameInstant(ZoneId.of("UTC")));
                        return loc;
                    });

            // 4. Поездка
            Trip trip = tripRepository.findById(dto.getTripId())
                    .orElseGet(Trip::new);

            trip.setVehicle(vehicle);
            trip.setBegin(ZonedDateTime.parse(dto.getBegin()).withZoneSameInstant(ZoneId.of("UTC")));
            trip.setEnd(ZonedDateTime.parse(dto.getEnd()).withZoneSameInstant(ZoneId.of("UTC")));
            trip.setBeginLocation(start);
            trip.setEndLocation(end);

            vehicle.getTrips().add(trip);
            return trip;
        };
    }

    @Bean
    public ItemWriter<Trip> tripWriter() {
        return items -> {
            System.out.println("Start writing " + items.size() + " trips to DB");
//            Enterprise enterprise = items.getItems().get(0).getVehicle().getEnterprise();
//            System.out.println(enterprise.getVehicles().get(0).getTrips().size());
//            enterpriseRepository.save(enterprise);
            for (Trip t : items) {
                System.out.println(t.getBeginLocation().getId());
                System.out.println(t.getEndLocation().getId());
                tripRepository.save(t);
            }
        };
    }

}
