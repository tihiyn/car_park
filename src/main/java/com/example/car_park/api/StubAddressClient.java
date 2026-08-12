package com.example.car_park.api;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Profile("e2e")
public class StubAddressClient implements AddressClient {

    @Override
    public String getAddressByCoords(double longitude, double latitude) {
        return String.format(Locale.US, "Тестовый адрес [%.4f, %.4f]", latitude, longitude);
    }
}
