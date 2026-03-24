package com.example.car_park.controllers.providers;

import com.example.car_park.controllers.dto.response.DriverDto;
import com.example.car_park.controllers.dto.response.VehicleEditDto;
import com.example.car_park.dao.DriverRepository;
import com.example.car_park.dao.mapper.DriverMapper;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Manager;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverProvider {
    private final DriverService s;
    private final DriverRepository r;
    private final DriverMapper dm;
    private final ManagerProvider mp;

    public List<DriverDto> findAllForRest(User u, Pageable p) {
        return r.findAllByEnterpriseIn(u.getManager().getManagedEnterprises(), p).stream()
            .map(dm::driverToDriverDto)
            .toList();
    }

    public DriverDto findByIdForRest(User u, Long id) {
        r.findById(id).orElseThrow(() -> {
            log.error("Водитель с id={} отсутствует", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Водитель с id=%d отсутствует", id));
        });
        return dm.driverToDriverDto(s.getIfBelongs(mp.getManagerByUser(u), id));
    }

    public List<VehicleEditDto.DriverEditDto> findAllFromEnterprise(Long eId) {
        return r.findAllByEnterpriseId(eId).stream()
            .map(dm::toEdit)
            .toList();
    }

    public List<VehicleEditDto.DriverEditDto> findAllWithoutActiveVehicle(Long eId) {
        return s.findAllWithoutActiveVehicle(r.findAllByEnterpriseId(eId)).stream()
            .map(dm::toEdit)
            .toList();
    }

    public List<Driver> findAllByIds(User u, Set<Long> ids) {
        List<Driver> all = r.findAllById(ids);
        s.checkAllExists(all, ids);
        List<Driver> managed = mp.getManagerByUser(u)
            .getManagedEnterprises().stream()
            .flatMap(e -> e.getDrivers().stream())
            .filter(d -> ids.contains(d.getId()))
            .toList();
        s.checkAllBelongs(managed, ids);
        return managed;
    }

    public Driver findByIdIn(List<Driver> drivers, Long id) {
        return s.findByIdIn(drivers, id);
    }
}
