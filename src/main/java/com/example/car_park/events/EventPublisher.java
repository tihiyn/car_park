package com.example.car_park.events;

import com.example.car_park.config.KafkaConfig;
import com.example.car_park.controllers.dto.response.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTripCreated(Notification event) {
        kafkaTemplate.send(KafkaConfig.TRIP_CREATED_RAW_TOPIC, event.getRegNum(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Не удалось опубликовать Notification для regNum={}", event.getRegNum(), ex);
                } else {
                    log.info("Опубликовано сообщение о поездке авто с номером {} в топик {}", event.getRegNum(),
                        KafkaConfig.TRIP_CREATED_RAW_TOPIC);
                }
            });
    }
}
