package com.example.car_park.dao;

import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VehicleCachedRepository {
    private final VehicleRepository r;

    @Cacheable(value = "vehicle", unless = "#result == null")
    public Vehicle findById(Long id) {
        return r.findById(id).orElseThrow(() -> {
            log.error("Транспортное средство с id={} отсутствует", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Транспортное средство с id=%d отсутствует", id));
        });
    }

    @CachePut(value = "vehicle", key = "v.id")
    public Vehicle update(Vehicle v) {
        return r.save(v);
    }

    @CacheEvict(value = "vehicle", key = "#v.id")
    public void delete(Vehicle v) {
        r.delete(v);
    }
}
