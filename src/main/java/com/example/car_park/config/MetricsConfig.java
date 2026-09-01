package com.example.car_park.config;

import com.example.car_park.dao.EnterpriseRepository;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.VehicleRepository;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    // Без этих бинов аннотации @Timed и @Observed молча игнорируются:
    // аспекты не поднимаются автоматически
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    // Общий тег, чтобы при возврате backend-2 метрики реплик не сливались в одну серию
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(
        @Value("${INSTANCE_ID:backend-1}") String instanceId) {
        return registry -> registry.config()
            .meterFilter(MeterFilter.commonTags(Tags.of("instance_id", instanceId)));
    }

    // Бизнес-метрики: размер парка снимается с БД на каждый скрейп.
    // count() — дешёвый запрос, скрейп идёт раз в 15 секунд.
    // Имена без суффикса .total: клиент Prometheus всё равно срезает _total у гейджей,
    // и в итоге серии называются car_park_vehicles / car_park_enterprises / car_park_trips
    @Bean
    public MeterBinder carParkBusinessMetrics(VehicleRepository vehicles,
                                              EnterpriseRepository enterprises,
                                              TripRepository trips) {
        return registry -> {
            Gauge.builder("car_park.vehicles", vehicles, r -> r.count())
                .description("Количество транспортных средств в парке")
                .register(registry);
            Gauge.builder("car_park.enterprises", enterprises, r -> r.count())
                .description("Количество предприятий")
                .register(registry);
            Gauge.builder("car_park.trips", trips, r -> r.count())
                .description("Количество сохранённых поездок")
                .register(registry);
        };
    }
}
