package com.example.car_park.controllers.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

@Data
@Accessors(chain = true)
public class ExportResp {
    private Resource res;
    private String fileName;
    private Long fileLength;
}
