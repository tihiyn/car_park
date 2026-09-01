package com.example.car_park.api;

import com.example.car_park.api.response.GeoapifyResponse;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("!e2e")
@RequiredArgsConstructor
@Slf4j
public class GeoapifyAddressClient implements AddressClient {
    private final RestTemplate restTemplate;

    @Value("${geoapify.api.key}")
    private String apiKey;
    @Value("${geoapify.api.url}")
    private String baseUrl;
    private static final String FORMAT = "json";

    // TODO: реализовать получение адресов батчами
    @Override
    @Timed(value = "geoapify.address.lookup", description = "Обращение к Geoapify за адресом по координатам")
    @Cacheable(value = "getAddressByCoords", unless = "#result == null")
    public String getAddressByCoords(double longitude, double latitude) {
        log.info("Обращение к Geoapify для получения адреса: широта={}, долгота={}", latitude, longitude);
        final String url = String.format("%slat=%s&lon=%s&format=%s&apiKey=%s",
                baseUrl,
                latitude,
                longitude,
                FORMAT,
                apiKey);
        GeoapifyResponse response = restTemplate.getForObject(url, GeoapifyResponse.class);
        if (response.getResults().isEmpty()) {
            log.error("Ошибка при обращении к Geoapify");
            throw new RuntimeException("Пустой ответ от Geoapify");
        }
        GeoapifyResponse.Result info = response.getResults().getFirst();
        log.info("Для [широта={}, долгота={}] Geoapify вернул {}", latitude, longitude, info);
        return info.toString();
    }
}
