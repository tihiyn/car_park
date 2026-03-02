package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.providers.ReportProvider;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportRestController {
    private final ReportProvider rp;

    @GetMapping("vehicle/mileage")
    public ResponseEntity<VehicleMileageReport> getVehicleMileageReport(@AuthenticationPrincipal User u,
                                                  @RequestParam("regNum") String regNum,
                                                  @RequestParam("period") Period p,
                                                  @RequestParam("begin") ZonedDateTime b,
                                                  @RequestParam("end") ZonedDateTime e) {
        VehicleMileageReport report = rp.buildVehicleMileageReport(u, regNum, p, b, e);
        return ResponseEntity.ok(report);
    }
}
