package com.example.car_park.enums;

import lombok.Getter;

@Getter
public enum Transmission {
    AUTOMATIC("Автоматическая"),
    MECHANICAL("Механическая"),
    ROBOTIC("Роботизированная"),
    VARIATOR("Вариатор"),
    NONE(null);

    private final String value;

    Transmission(String value) {
        this.value = value;
    }
}
