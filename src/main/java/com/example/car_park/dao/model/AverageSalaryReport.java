package com.example.car_park.dao.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AverageSalaryReport extends Report<Map<String, Integer>>{
    private Long enterpriseId;
}
