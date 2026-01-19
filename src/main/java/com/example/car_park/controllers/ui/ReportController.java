package com.example.car_park.controllers.ui;

import com.example.car_park.controllers.providers.ReportProvider;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.ZonedDateTime;

@Controller
@RequestMapping("/api/ui/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportProvider rp;

    @GetMapping("/create")
    public String getCreationForm(@AuthenticationPrincipal User u, Model m) {
        m.addAttribute("vehicles", rp.findAllAvailVehicles(u));
        m.addAttribute("enterprises", rp.findAllAvailEnterprises(u));
        return "create_report";
    }

    @GetMapping("vehicle/mileage")
    public String getVehicleMileageReport(@AuthenticationPrincipal User u,
                                          @RequestParam("id") Long vId,
                                          @RequestParam("period") Period p,
                                          @RequestParam("begin") ZonedDateTime b,
                                          @RequestParam("end") ZonedDateTime e,
                                          Model m) {
        VehicleMileageReport report = rp.buildVehicleMileageReport(u, vId, p, b, e);
        m.addAttribute("report", report);
        return "vehicle_mileage_report";
    }

    @GetMapping("/enterprise/production")
    public String getProductionYearReport(@AuthenticationPrincipal User u,
                                          @RequestParam("id") Long eId,
                                          @RequestParam("beginYear") Integer sYear,
                                          @RequestParam("endYear") Integer bYear,
                                          Model m) {
        m.addAttribute("report", rp.buildProductionYearReport(u, eId, sYear, bYear));
        return "production_year_report";
    }

    @GetMapping("/enterprise/salary")
    public String getAverageSalaryInEnterprise(@AuthenticationPrincipal User u,
                                               @RequestParam("id") Long eId,
                                               Model m) {
        m.addAttribute("report", rp.buildAverageSalaryReport(u, eId));
        return "average_salary_report";
    }

    @GetMapping("vehicle/mileage/export")
    public void exportVehicleMileageReport(@AuthenticationPrincipal User u,
                                           @RequestParam("id") Long vId,
                                           @RequestParam("period") Period p,
                                           @RequestParam("begin") ZonedDateTime b,
                                           @RequestParam("end") ZonedDateTime e,
                                           HttpServletResponse r) {
        r.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        r.setHeader("Content-Disposition", "attachment; filename=vehicle_mileage_report.xlsx");
        Workbook wb = rp.exportVehicleMileageReport(u, vId, p, b, e);
        try {
            wb.write(r.getOutputStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/enterprise/production/export")
    public void exportProductionYearReport(@AuthenticationPrincipal User u,
                                           @RequestParam("id") Long eId,
                                           @RequestParam("beginYear") Integer bYear,
                                           @RequestParam("endYear") Integer eYear,
                                           HttpServletResponse r) {
        r.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        r.setHeader("Content-Disposition", "attachment; filename=enterprise_production_report.xlsx");
        Workbook wb = rp.exportProductionYearReport(u, eId, bYear, eYear);
        try {
            wb.write(r.getOutputStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/enterprise/salary/export")
    public void exportAverageSalaryInEnterprise(@AuthenticationPrincipal User u,
                                                @RequestParam("id") Long eId,
                                                HttpServletResponse r) {
        r.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        r.setHeader("Content-Disposition", "attachment; filename=enterprise_salary_report.xlsx");
        Workbook wb = rp.exportAverageSalaryReport(u, eId);
        try {
            wb.write(r.getOutputStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
