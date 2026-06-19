package com.example.car_park.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String TRIP_CREATED_RAW_TOPIC = "car-park.trip.created.raw.v1";

    @Bean
    public NewTopic tripCreatedRawTopic() {
        return TopicBuilder.name(TRIP_CREATED_RAW_TOPIC)
            .partitions(1)
            .replicas(1)
            .build();
    }
}
