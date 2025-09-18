package com.example.car_park.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Period {
    DAY ("по дням"),
    MONTH("по месяцам"),
    YEAR("по годам");

    private final String name;
}
