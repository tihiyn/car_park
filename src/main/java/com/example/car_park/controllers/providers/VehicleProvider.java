package com.example.car_park.controllers.providers;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleCreateDto;
import com.example.car_park.controllers.dto.response.VehicleEditDto;
import com.example.car_park.controllers.dto.response.VehicleInfoViewModel;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.VehicleCachedRepository;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.mapper.VehicleMapper;
import com.example.car_park.dao.model.Brand;
import com.example.car_park.dao.model.Driver;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleProvider {
    private final VehicleService s;
    private final VehicleRepository r;
    private final VehicleCachedRepository cr;
    private final VehicleMapper m;
    private final ManagerProvider mp;
    private final BrandProvider bp;
    private final DriverProvider dp;
    private final EnterpriseProvider ep;

    // TODO: проверить кол-во генерируемых запросов к БД
    public List<VehicleInfoViewModel> findAllForUI(User u) {
        return s.findAllByManager(mp.getManagerByUser(u)).stream()
            .map(m::toModel)
            .toList();
    }

    public List<VehicleResponseDto> findAllForRest(User u, Pageable p) {
        return r.findAllByEnterpriseIn(u.getManager().getManagedEnterprises(), p).stream()
            .map(m::vehicleToVehicleResponseDto)
            .toList();
    }

    public List<Vehicle> findAllByIds(User u, Set<Long> ids) {
        List<Vehicle> all = r.findAllById(ids);
        s.checkAllExists(all, ids);
        List<Vehicle> managed = mp.getManagerByUser(u)
            .getManagedEnterprises().stream()
            .flatMap(e -> e.getVehicles().stream())
            .filter(v -> ids.contains(v.getId()))
            .toList();
        s.checkAllBelongs(managed, ids);
        return managed;
    }

    public VehicleInfoViewModel findByIdForUI(User u, Long id) {
        return m.toModel(findById(u,  id));
    }

    public VehicleResponseDto findByIdForRest(User u, Long id) {
        return m.vehicleToVehicleResponseDto(findById(u, id));
    }

    public Vehicle findById(User u, Long id) {
        Vehicle v = cr.findById(id);
        if (!r.isBelongManagedEnterprises(id, u.getId())) {
            throw new ResponseStatusException(FORBIDDEN,
                String.format("Транспортное средство с id=%d не относится к Вашим предприятиям", id));
        }
        return v;
    }

    public Vehicle findByIdAttached(Long id) {
        return r.findById(id).orElseThrow(() ->
            new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                String.format("Транспортное средство с id=%d отсутствует", id)));
    }

    public Vehicle findByRegNum(User u, String regNum) {
        Optional<Vehicle> ov = r.findByRegNum(regNum);
        if (ov.isPresent()) {
            return s.getIfBelongs(mp.getManagerByUser(u), ov.get().getId());
        }
        log.error("Транспортное средство с номером {} отсутствует", regNum);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Транспортное средство с номером=%s отсутствует", regNum));
    }

    public VehicleEditDto edit(User u, Long id) {
        try {
            Vehicle v = s.getIfBelongs(mp.getManagerByUser(u), cr.findById(id).getId());
            return m.toEdit(v);
        } catch (ResponseStatusException e) {
            return null;
        }
    }

    public List<VehicleEditDto.BrandEditDto> getBrands() {
        return bp.findAll();
    }

    public List<VehicleEditDto.DriverEditDto> findAllDriversFromEnterprise(Long vId) {
        return dp.findAllFromEnterprise(cr.findById(vId).getEnterprise().getId());
    }

    public List<VehicleEditDto.DriverEditDto> findAllDriversWithoutActiveVehicle(Long vId) {
        return dp.findAllWithoutActiveVehicle(cr.findById(vId).getEnterprise().getId());
    }

    public VehicleCreateDto prepareToCreate(User u, Long eId) {
        Enterprise e = ep.findById(u, eId);
        return new VehicleCreateDto()
            .setEnterpriseId(e.getId());
    }

    public List<VehicleEditDto.DriverEditDto> findAllDriversByEnterprise(Long eId) {
        return dp.findAllFromEnterprise(eId);
    }

    public List<VehicleEditDto.DriverEditDto> findAllDriversByEnterpriseWithoutActiveVehicle(Long eId) {
        return dp.findAllWithoutActiveVehicle(eId);
    }

    @Transactional
    public void save(User u, VehicleCreateDto dto) {
        Brand b = bp.findById(dto.getBrand().getId());
        Enterprise e = ep.findById(u, dto.getEnterpriseId());
        List<Driver> ds = dto.getDrivers() == null ? List.of() : dp.findAllByIds(u, dto.getDrivers().stream()
            .map(VehicleEditDto.DriverEditDto::getId)
            .collect(Collectors.toSet()));
        Driver ad = findActiveDriverAmong(ds, dto.getActiveDriver() == null ? null : dto.getActiveDriver().getId());
        Vehicle v = m.createDtoToEntity(dto, b, e, ds, ad);
        // Обратные стороны связей (Brand.vehicles, Driver.vehicles, Driver.activeVehicle)
        // руками проставлять не нужно: владелец всех трёх связей — Vehicle
        r.save(v);
    }

    private Driver findActiveDriverAmong(List<Driver> ds, Long activeDriverId) {
        if (activeDriverId == null) {
            return null;
        }
        return ds.stream()
            .filter(d -> d.getId().equals(activeDriverId))
            .findAny()
            .orElseThrow(() -> {
                log.error("Активный водитель с id={} не принадлежит списку назначенных водителей",
                    activeDriverId);
                return new ResponseStatusException(BAD_REQUEST,
                    "Активный водитель должен быть из списка назначенных водителей");
            });
    }

    @Transactional
    public Long create(User u, VehicleRequestDto dto) {
        Brand b = bp.findById(dto.getBrandId());
        Enterprise e = ep.findById(u, dto.getEnterpriseId());
        List<Driver> ds = dp.findAllByIds(u, dto.getDriverIds());
        if (dto.getActiveDriverId() != null &&
            !dto.getDriverIds().contains(dto.getActiveDriverId())) {
            log.error("Ошибка при создании транспортного средства: активный водитель с id={} не принадлежит списку назначенных водителей {}",
                dto.getActiveDriverId(), dto.getDriverIds());
            throw new ResponseStatusException(BAD_REQUEST,
                "Активный водитель должен быть из списка назначенных водителей");
        }
        // TODO: проверить, что будет если назначить водителя, который активен на другой машине
        Driver ad = findActiveDriverAmong(ds, dto.getActiveDriverId());
        Vehicle v = m.vehicleRequestDtoToVehicle(dto, b, e, ds, ad);
        // Обратные стороны связей проставляются владельцем — самим Vehicle
        return r.save(v).getId();
    }

    @Transactional
    public void update(User u, VehicleEditDto dto) {
        // проверяем права, но работаем с управляемой сущностью: findById отдаёт
        // ТС из кэша, у него ленивые связи уже отвязаны от сессии
        findById(u, dto.getId());
        Vehicle existing = findByIdAttached(dto.getId());
        m.editDtoToEntity(dto, existing);
        if (!existing.getBrand().getId().equals(dto.getBrand().getId())) {
            Brand b = bp.findById(dto.getBrand().getId());
            existing.setBrand(b);
        }
        Set<Long> updDriverIds = dto.getDrivers() == null ? Set.of() : dto.getDrivers().stream()
            .map(VehicleEditDto.DriverEditDto::getId)
            .collect(Collectors.toSet());
        if (!existing.getDrivers().stream()
            .map(Driver::getId)
            .collect(Collectors.toSet()).equals(updDriverIds)) {
            List<Driver> updDrivers = dp.findAllByIds(u, updDriverIds);
            existing.setDrivers(updDrivers);
        }
        Long existingActiveDriverId = existing.getActiveDriver() == null ? null : existing.getActiveDriver().getId();
        Long updActiveDriverId = dto.getActiveDriver() == null ? null : dto.getActiveDriver().getId();
        if (!Objects.equals(existingActiveDriverId, updActiveDriverId)) {
            existing.setActiveDriver(updActiveDriverId == null
                ? null
                : dp.findByIdIn(existing.getDrivers(), updActiveDriverId));
        }
        cr.update(existing);
    }

    @Transactional
    public VehicleResponseDto edit(User u, Long id, VehicleRequestDto dto) {
        Vehicle existing = findById(u, id);
        m.vehicleRequestDtoToVehicle(dto, existing);
        if (!existing.getBrand().getId().equals(dto.getBrandId())) {
            Brand brand = bp.findById(dto.getBrandId());
            existing.setBrand(brand);
        }
        if (!existing.getEnterprise().getId().equals(dto.getEnterpriseId())) {
            Enterprise enterprise = ep.findById(u, dto.getEnterpriseId());
            existing.setEnterprise(enterprise);
        }
        if (!existing.getDrivers().stream()
            .map(Driver::getId)
            .collect(Collectors.toSet()).equals(dto.getDriverIds())) {
            List<Driver> updatedDriverList = dp.findAllByIds(u, dto.getDriverIds());
            existing.setDrivers(updatedDriverList);
        }
        if (dto.getActiveDriverId() != null &&
            !dto.getDriverIds().contains(dto.getActiveDriverId())) {
            log.error("Ошибка при редактировании транспортного средства: активный водитель с id={} не принадлежит списку назначенных водителей {}",
                dto.getActiveDriverId(), dto.getDriverIds());
            throw new ResponseStatusException(BAD_REQUEST,
                "Активный водитель должен быть из списка назначенных водителей");
        }
        // TODO: проверить, что будет если назначить водителя, который активен на другой машине
        Long existingActiveDriverId = existing.getActiveDriver() == null ? null : existing.getActiveDriver().getId();
        if (!Objects.equals(existingActiveDriverId, dto.getActiveDriverId())) {
            Driver ad = dto.getActiveDriverId() == null ? null : existing.getDrivers().stream()
                .filter(driver -> driver.getId().equals(dto.getActiveDriverId()))
                .findAny()
                .orElse(null);
            existing.setActiveDriver(ad);
        }
        return m.vehicleToVehicleResponseDto(cr.update(existing));
    }

    @Transactional
    public void delete(User u, Long id) {
        findById(u, id);
        Vehicle v = findByIdAttached(id);
        // Снимаем связи с обеих сторон: владелец — Vehicle, но пока в сессии висит
        // Driver.activeVehicle, указывающий на удаляемую машину, flush падает
        Driver ad = v.getActiveDriver();
        if (ad != null) {
            ad.setActiveVehicle(null);
            v.setActiveDriver(null);
        }
        v.getDrivers().forEach(d -> d.getVehicles().remove(v));
        v.getDrivers().clear();
        cr.delete(v);
    }
}
