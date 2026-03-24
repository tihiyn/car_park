package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final WebClient wc;

    @Value("${bot.notify.url}")
    private String url;

    public Mono<Void> sendNotification(Notification n) {
        log.info("Отправка нотификации менеджерам с id {} о добавлении поездки на авто с номером {}",
            n.getManagers(), n.getRegNum());
        return wc.post()
            .uri(url)
            .bodyValue(n)
            .retrieve()
            .toBodilessEntity()
            .then();
    }
}
