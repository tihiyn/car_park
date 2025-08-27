package com.example.car_park.api;

import com.example.car_park.api.response.GeoapifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GeoapifyAddressClient implements AddressClient {
    private final RestTemplate restTemplate;

    @Value("${geoapify.api.key}")
    private String apiKey;
    @Value("${geoapify.api.url}")
    private String baseUrl;
    private static final String FORMAT = "json";

    // TODO: реализовать получение адресов батчами
    @Override
    public String getAddressByCoords(double longitude, double latitude) {
        final String url = String.format("%slat=%s&lon=%s&format=%s&apiKey=%s",
                baseUrl,
                latitude,
                longitude,
                FORMAT,
                apiKey);
        GeoapifyResponse response = restTemplate.getForObject(url, GeoapifyResponse.class);
        if (response.getResults().isEmpty()) {
            throw new RuntimeException("Пустой ответ от Geoapify");
        }
        GeoapifyResponse.Result info = response.getResults().getFirst();
        return info.toString();
    }
}
