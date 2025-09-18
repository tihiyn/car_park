package com.example.car_park.controllers;

import com.example.car_park.dao.model.AverageSalaryReport;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.ProductionYearReport;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import com.example.car_park.service.EnterpriseService;
import com.example.car_park.service.ReportService;
import com.example.car_park.service.VehicleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/ui/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;

    @GetMapping("/create")
    public String getCreationForm(@AuthenticationPrincipal User user, Model model) {
        List<Vehicle> availVehicles = vehicleService.findAllByUser(user);
        List<Enterprise> availEnterprises = enterpriseService.findAll(user, null);
        model.addAttribute("vehicles", availVehicles);
        model.addAttribute("enterprises", availEnterprises);
        return "create_report";
    }

    @GetMapping("vehicle/mileage")
    public String getVehicleMileageReport(@AuthenticationPrincipal User user,
                                          @RequestParam("id") Long vehicleId,
                                          @RequestParam Period period,
                                          @RequestParam ZonedDateTime begin,
                                          @RequestParam ZonedDateTime end,
                                          Model model) {
        VehicleMileageReport report = reportService.generateVehicleMileageReport(user, vehicleId, period, begin, end);
        model.addAttribute("report", report);
        return "vehicle_mileage_report";
    }

    @GetMapping("/enterprise/production")
    public String getProductionYearReport(@AuthenticationPrincipal User user,
                                          @RequestParam("id") Long enterpriseId,
                                          @RequestParam Integer beginYear,
                                          @RequestParam Integer endYear,
                                          Model model) {
        ProductionYearReport report = reportService.generateProductionYearReport(user, enterpriseId, beginYear, endYear);
        model.addAttribute("report", report);
        return "production_year_report";
    }

    @GetMapping("/enterprise/salary")
    public String getAverageSalaryInEnterprise(@AuthenticationPrincipal User user,
                                          @RequestParam("id") Long enterpriseId,
                                          Model model) {
        AverageSalaryReport report = reportService.generateAverageSalaryReport(user, enterpriseId);
        model.addAttribute("report", report);
        return "average_salary_report";
    }

    @GetMapping("vehicle/mileage/export")
    public void exportVehicleMileageReport(@AuthenticationPrincipal User user,
                                           @RequestParam("id") Long vehicleId,
                                           @RequestParam Period period,
                                           @RequestParam ZonedDateTime begin,
                                           @RequestParam ZonedDateTime end,
                                           HttpServletResponse response) throws IOException {
        VehicleMileageReport report = reportService.generateVehicleMileageReport(user, vehicleId, period, begin, end);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=vehicle_mileage_report.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчёт");
            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.createCell(0).setCellValue(report.getName());

            Row row1 = sheet.createRow(rowIdx++);
            row1.createCell(0).setCellValue("Период:");
            row1.createCell(1).setCellValue(report.getPeriod().getName());

            Row row2 = sheet.createRow(rowIdx++);
            row2.createCell(0).setCellValue("Начало:");
            row2.createCell(1).setCellValue(report.getBegin().toString());

            Row row3 = sheet.createRow(rowIdx++);
            row3.createCell(0).setCellValue("Конец:");
            row3.createCell(1).setCellValue(report.getEnd().toString());

            rowIdx++;

            Row tableHeader = sheet.createRow(rowIdx++);
            tableHeader.createCell(0).setCellValue("Период");
            tableHeader.createCell(1).setCellValue("Пробег");

            for (var entry : report.getResult().entrySet()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().toString());
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/enterprise/production/export")
    public void exportProductionYearReport(@AuthenticationPrincipal User user,
                                           @RequestParam("id") Long enterpriseId,
                                           @RequestParam Integer beginYear,
                                           @RequestParam Integer endYear,
                                           HttpServletResponse response) throws IOException {
        ProductionYearReport report = reportService.generateProductionYearReport(user, enterpriseId, beginYear, endYear);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=enterprise_production_report.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчёт");
            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.createCell(0).setCellValue(report.getName());

            Row row2 = sheet.createRow(rowIdx++);
            row2.createCell(0).setCellValue("Начало:");
            row2.createCell(1).setCellValue(report.getBeginYear());

            Row row3 = sheet.createRow(rowIdx++);
            row3.createCell(0).setCellValue("Конец:");
            row3.createCell(1).setCellValue(report.getEndYear());

            rowIdx++;

            Row tableHeader = sheet.createRow(rowIdx++);
            tableHeader.createCell(0).setCellValue("Год");
            tableHeader.createCell(1).setCellValue("Количество автомобилей");

            for (var entry : report.getResult().entrySet()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/enterprise/salary/export")
    public void exportAverageSalaryInEnterprise(@AuthenticationPrincipal User user,
                                                @RequestParam("id") Long enterpriseId,
                                                HttpServletResponse response) throws Exception {
        AverageSalaryReport report = reportService.generateAverageSalaryReport(user, enterpriseId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=enterprise_salary_report.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчёт");
            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.createCell(0).setCellValue(report.getName());

            Row tableHeader = sheet.createRow(rowIdx++);
            tableHeader.createCell(0).setCellValue("Предприятие");
            tableHeader.createCell(1).setCellValue("Средняя зарплата");

            for (var entry : report.getResult().entrySet()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(response.getOutputStream());
        }
    }
}
