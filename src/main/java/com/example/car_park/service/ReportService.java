package com.example.car_park.service;

import com.example.car_park.dao.model.AverageSalaryReport;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.ProductionYearReport;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    public VehicleMileageReport buildVehicleMileageReport(Vehicle v, List<Trip> ts, Period p, ZonedDateTime s, ZonedDateTime b) {
        VehicleMileageReport r = new VehicleMileageReport();
        r.setName("Пробег автомобиля %s %s".formatted(v.getRegNum(), p.getName()));
        r.setVehicleId(v.getId());
        r.setPeriod(p);
        r.setBegin(s);
        r.setEnd(b);
        r.setResult(ts.stream()
            .collect(Collectors.toMap(
                trip -> getPeriod(trip.getBeginLocation().getTimestamp(), p),
                Trip::getLength,
                Long::sum)
            )
        );
        return r;
    }

    private String getPeriod(ZonedDateTime dt, Period period) {
        return switch (period) {
            case DAY -> dt.format(DateTimeFormatter.ISO_DATE);
            case MONTH -> dt.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"));
            case YEAR -> String.valueOf(dt.getYear());
        };
    }

    public ProductionYearReport buildProductionYearReport(Enterprise e, Integer sYear, Integer bYear) {
        Map<Integer, Long> res = e.getVehicles().stream()
            .map(Vehicle::getProductionYear)
            .filter(year -> year >= sYear && year <= bYear)
            .collect(Collectors.groupingBy(
                num -> num,
                Collectors.counting()
            )).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));
        ProductionYearReport r = new ProductionYearReport();
        r.setEnterpriseId(e.getId());
        r.setName("Статистика количества автомобилей по годам производства в предприятии \"%s\"".formatted(e.getName()));
        r.setBeginYear(sYear);
        r.setEndYear(bYear);
        r.setResult(res);
        return r;
    }

    public AverageSalaryReport buildAverageSalaryReport(Enterprise e) {
        Integer avgSalary = (int) e.getDrivers().stream()
            .mapToDouble(driver -> driver.getSalary().doubleValue())
            .average().orElse(0.0);
        AverageSalaryReport report = new AverageSalaryReport();
        report.setEnterpriseId(e.getId());
        report.setName("Средняя зарплата водителей в предприятии \"%s\"".formatted(e.getName()));
        report.setResult(Map.of(e.getName(), avgSalary));
        return report;
    }

    public Workbook fillVehicleMileageReportFile(VehicleMileageReport r, Workbook wb) {
        Sheet sheet = wb.createSheet("Отчёт");
        int rowIdx = 0;
        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.createCell(0).setCellValue(r.getName());
        Row row1 = sheet.createRow(rowIdx++);
        row1.createCell(0).setCellValue("Период:");
        row1.createCell(1).setCellValue(r.getPeriod().getName());
        Row row2 = sheet.createRow(rowIdx++);
        row2.createCell(0).setCellValue("Начало:");
        row2.createCell(1).setCellValue(r.getBegin().toString());
        Row row3 = sheet.createRow(rowIdx++);
        row3.createCell(0).setCellValue("Конец:");
        row3.createCell(1).setCellValue(r.getEnd().toString());
        rowIdx++;
        Row tableHeader = sheet.createRow(rowIdx++);
        tableHeader.createCell(0).setCellValue("Период");
        tableHeader.createCell(1).setCellValue("Пробег");
        for (var entry : r.getResult().entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().toString());
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        return wb;
    }

    public Workbook fillProductionYearReportFile(ProductionYearReport r, Workbook wb) {
        Sheet sheet = wb.createSheet("Отчёт");
        int rowIdx = 0;
        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.createCell(0).setCellValue(r.getName());
        Row row2 = sheet.createRow(rowIdx++);
        row2.createCell(0).setCellValue("Начало:");
        row2.createCell(1).setCellValue(r.getBeginYear());
        Row row3 = sheet.createRow(rowIdx++);
        row3.createCell(0).setCellValue("Конец:");
        row3.createCell(1).setCellValue(r.getEndYear());
        rowIdx++;
        Row tableHeader = sheet.createRow(rowIdx++);
        tableHeader.createCell(0).setCellValue("Год");
        tableHeader.createCell(1).setCellValue("Количество автомобилей");
        for (var entry : r.getResult().entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        return wb;
    }

    public Workbook fillAverageSalaryReportFile(AverageSalaryReport r, Workbook wb) {
        Sheet sheet = wb.createSheet("Отчёт");
        int rowIdx = 0;
        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.createCell(0).setCellValue(r.getName());
        Row tableHeader = sheet.createRow(rowIdx++);
        tableHeader.createCell(0).setCellValue("Предприятие");
        tableHeader.createCell(1).setCellValue("Средняя зарплата");
        for (var entry : r.getResult().entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        return wb;
    }
}
