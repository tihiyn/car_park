package com.example.car_park.service;

import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.model.AverageSalaryReport;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.ProductionYearReport;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ReportService {
    private final VehicleService vehicleService;
    private final EnterpriseService enterpriseService;
    private final TripRepository tripRepository;

    public VehicleMileageReport generateVehicleMileageReport(User user, Long vehicleId, Period period, ZonedDateTime begin, ZonedDateTime end) {
        Vehicle vehicle = vehicleService.findById(user, vehicleId);
        List<Trip> trips = tripRepository.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(vehicle, begin, end);
        VehicleMileageReport report = new VehicleMileageReport();
        report.setName(String.format("Пробег автомобиля %s %s", vehicle.getRegNum(), period.getName()));
        report.setVehicleId(vehicleId);
        report.setPeriod(period);
        report.setBegin(begin);
        report.setEnd(end);
        report.setResult(trips.stream()
                .collect(Collectors.toMap(
                        trip -> getPeriod(trip.getBeginLocation().getTimestamp(), period),
                        Trip::getLength,
                        Long::sum)
                ));
        return report;
    }

    private String getPeriod(ZonedDateTime dt, Period period) {
        return switch (period) {
            case DAY -> dt.format(DateTimeFormatter.ISO_DATE);
            case MONTH -> dt.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"));
            case YEAR -> String.valueOf(dt.getYear());
        };
    }

    public ProductionYearReport generateProductionYearReport(User user, Long enterpriseId, Integer begin, Integer end) {
        Enterprise enterprise = enterpriseService.findById(user, enterpriseId);
        Map<Integer, Long> result = enterprise.getVehicles().stream()
                .map(Vehicle::getProductionYear)
                .filter(year -> year >= begin && year <= end)
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
        ProductionYearReport report = new ProductionYearReport();
        report.setEnterpriseId(enterpriseId);
        report.setName(String.format("Статистика количества автомобилей по годам производства в предприятии \"%s\"", enterprise.getName()));
        report.setBeginYear(begin);
        report.setEndYear(end);
        report.setResult(result);
        return report;
    }

    public AverageSalaryReport generateAverageSalaryReport(User user, Long enterpriseId) {
        Enterprise enterprise = enterpriseService.findById(user, enterpriseId);
        Integer avgSalary = (int) enterprise.getDrivers().stream()
                .mapToDouble(driver -> driver.getSalary().doubleValue())
                .average().orElse(0.0);
        AverageSalaryReport report = new AverageSalaryReport();
        report.setEnterpriseId(enterpriseId);
        report.setName(String.format("Средняя зарплата водителей в предприятии \"%s\"", enterprise.getName()));
        report.setResult(Map.of(enterprise.getName(), avgSalary));
        return report;
    }

    private String origin;

    public List<Boolean> customMap(List<String> strings) {
        return strings.stream()
                .map(s -> origin.contains(s))
                .toList();
    }
}
