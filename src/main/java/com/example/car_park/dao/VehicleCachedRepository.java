package com.example.car_park.dao;

import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VehicleCachedRepository {
    private final VehicleRepository r;

    // Грузим ТС вместе со связями: в кэш должна попасть самодостаточная
    // сущность, иначе первое же обращение к ленивому полю вне сессии
    // упадёт с LazyInitializationException
    @Cacheable(value = "vehicle", unless = "#result == null")
    public Vehicle findById(Long id) {
        return r.findByIdWithAssociations(id).orElseThrow(() -> {
            log.error("Транспортное средство с id={} отсутствует", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Транспортное средство с id=%d отсутствует", id));
        });
    }

    // Именно evict, а не put: в кэш попала бы сущность с ленивыми прокси
    // (Enterprise, Brand), и первое же чтение вне сессии падало бы с
    // LazyInitializationException. Следующий findById перечитает ТС из БД.
    @CacheEvict(value = "vehicle", key = "#v.id")
    public Vehicle update(Vehicle v) {
        return r.save(v);
    }

    @CacheEvict(value = "vehicle", key = "#v.id")
    public void delete(Vehicle v) {
        r.delete(v);
    }
}
