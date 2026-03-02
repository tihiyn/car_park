package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final WebClient wc;

    @Value("${bot.notify.url}")
    private String url;

    public Mono<Void> sendNotification(Notification n) {
        return wc.post()
            .uri(url)
            .bodyValue(n)
            .retrieve()
            .toBodilessEntity()
            .then();
    }
}
