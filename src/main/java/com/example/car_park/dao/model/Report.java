package com.example.car_park.dao.model;

import com.example.car_park.enums.Period;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class Report<T> {
    private String name;
    private Period period;
    private ZonedDateTime begin;
    private ZonedDateTime end;
    private T result;
}
