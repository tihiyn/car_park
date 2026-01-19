package com.example.car_park.controllers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportResp {
    private boolean isSuccessful;
    private String desc;
}
