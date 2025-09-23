package com.example.car_park.dao.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProductionYearReport extends Report<Map<Integer, Long>> {
    private Long enterpriseId;
    private Integer beginYear;
    private Integer endYear;
}
