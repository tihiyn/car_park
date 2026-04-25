package com.example.car_park.controllers.providers;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.controllers.dto.response.EnterpriseViewModel;
import com.example.car_park.dao.EnterpriseRepository;
import com.example.car_park.dao.mapper.EnterpriseMapper;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.service.EnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnterpriseProvider {
    private final EnterpriseService s;
    private final EnterpriseRepository r;
    private final EnterpriseMapper m;
    private final VehicleProvider vp;
    private final DriverProvider dp;

    // TODO избавиться от циклической зависимости между EP и VP
    public EnterpriseProvider(EnterpriseService s,
                              EnterpriseRepository r,
                              EnterpriseMapper m,
                              @Lazy VehicleProvider vp,
                              DriverProvider dp) {
        this.s = s;
        this.r = r;
        this.m = m;
        this.vp = vp;
        this.dp = dp;
    }

    public List<EnterpriseViewModel> findAllForUI(User u, Pageable p) {
        return r.findAllByManagersContaining(u.getManager(), p).getContent().stream()
            .map(m::toModel)
            .toList();
    }

    public List<EnterpriseResponseDto> findAllForRest(User u, Pageable p) {
        return r.findAllByManagersContaining(u.getManager(), p).getContent().stream()
            .map(m::enterpriseToEnterpriseResponseDto)
            .toList();
    }

    public Enterprise findById(User u, Long id) {
        r.findById(id).orElseThrow(() -> {
            log.error("Предприятие с id={} отсутствует", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Предприятие с id=%d отсутствует", id));
        });
        return s.getIfBelongs(u.getManager(), id);
    }

    public EnterpriseResponseDto findByIdForRest(User u, Long id) {
        return m.enterpriseToEnterpriseResponseDto(findById(u, id));
    }

    public List<String> getTimeZones() {
        return s.getTimeZones();
    }

    @Transactional
    public void updateTimeZone(User u, Long id, String tz) {
        Enterprise e = findById(u, id);
        e.setTimeZone(ZoneId.of(tz));
    }

    @Transactional
    public Long create(User u, EnterpriseRequestDto dto) {
        List<Vehicle> vs = vp.findAllByIds(u, dto.getVehicleIds());
        List<Driver> ds = dp.findAllByIds(u, dto.getDriverIds());
        List<Manager> ms = new ArrayList<>(List.of(u.getManager()));
        Enterprise e = m.enterpriseRequestDtoToEnterprise(dto, vs, ds, ms);
        vs.forEach(v -> v.setEnterprise(e));
        ds.forEach(d -> d.setEnterprise(e));
        ms.forEach(m -> m.getManagedEnterprises().add(e));
        return r.save(e).getId();
    }

    @Transactional
    public EnterpriseResponseDto edit(User u, Long id, EnterpriseRequestDto dto) {
        Enterprise existing = findById(u, id);
        m.enterpriseRequestDtoToEnterprise(dto, existing);
        if (!existing.getVehicles().stream()
            .map(Vehicle::getId)
            .collect(Collectors.toSet()).equals(dto.getVehicleIds())) {
            List<Vehicle> updVehicles = vp.findAllByIds(u, dto.getVehicleIds());
            existing.setVehicles(updVehicles);
        }
        if (!existing.getDrivers().stream()
            .map(Driver::getId)
            .collect(Collectors.toSet()).equals(dto.getDriverIds())) {
            List<Driver> updDrivers = dp.findAllByIds(u, dto.getDriverIds());
            existing.getDrivers().clear();
            existing.getDrivers().addAll(updDrivers);
        }
        return m.enterpriseToEnterpriseResponseDto(r.save(existing));
    }

    public void delete(User u, Long id) {
        Enterprise e = findById(u, id);
        u.getManager().getManagedEnterprises().remove(e);
        r.delete(e);
    }
}
