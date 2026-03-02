package com.example.car_park.controllers.providers;

import com.example.car_park.controllers.dto.response.EnterpriseViewModel;
import com.example.car_park.controllers.dto.response.VehicleInfoViewModel;
import com.example.car_park.dao.model.AverageSalaryReport;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.ProductionYearReport;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleMileageReport;
import com.example.car_park.enums.Period;
import com.example.car_park.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportProvider {
    private final ReportService rs;
    private final VehicleProvider vp;
    private final EnterpriseProvider ep;
    private final TripProvider tp;

    public List<VehicleInfoViewModel> findAllAvailVehicles(User u) {
        return vp.findAllForUI(u);
    }

    // FIXME: избавиться от передачи null в качестве аргумента
    public List<EnterpriseViewModel> findAllAvailEnterprises(User u) {
        return ep.findAllForUI(u, null);
    }

    public VehicleMileageReport buildVehicleMileageReport(User u, Long vId, Period p, ZonedDateTime s, ZonedDateTime b) {
        Vehicle v = vp.findById(u, vId);
        List<Trip> ts = tp.findInInterval(v, s, b);
        return rs.buildVehicleMileageReport(v, ts, p, s, b);
    }

    public VehicleMileageReport buildVehicleMileageReport(User u, String regNum, Period p, ZonedDateTime s, ZonedDateTime b) {
        Vehicle v = vp.findByRegNum(u, regNum);
        List<Trip> ts = tp.findInInterval(v, s, b);
        return rs.buildVehicleMileageReport(v, ts, p, s, b);
    }

    public Workbook exportVehicleMileageReport(User u, Long vId, Period p, ZonedDateTime s, ZonedDateTime b) {
        VehicleMileageReport r = buildVehicleMileageReport(u, vId, p, s, b);
        try (Workbook wb = new XSSFWorkbook()) {
            return rs.fillVehicleMileageReportFile(r, wb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductionYearReport buildProductionYearReport(User u, Long eId, Integer sYear, Integer bYear) {
        Enterprise e = ep.findById(u, eId);
        return rs.buildProductionYearReport(e, sYear, bYear);
    }

    public Workbook exportProductionYearReport(User u, Long eId, Integer sYear, Integer bYear) {
        ProductionYearReport r = buildProductionYearReport(u, eId, sYear, bYear);
        try (Workbook wb = new XSSFWorkbook()) {
            return rs.fillProductionYearReportFile(r, wb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AverageSalaryReport buildAverageSalaryReport(User u, Long eId) {
        Enterprise e = ep.findById(u, eId);
        return rs.buildAverageSalaryReport(e);
    }

    public Workbook exportAverageSalaryReport(User u, Long eId) {
        AverageSalaryReport r = buildAverageSalaryReport(u, eId);
        try (Workbook wb = new XSSFWorkbook()) {
            return rs.fillAverageSalaryReportFile(r, wb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
