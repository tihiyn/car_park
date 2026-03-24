package com.example.car_park.dao;

import com.example.car_park.dao.model.Brand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BrandCachedRepository {
    private final BrandRepository r;

    @Cacheable(value = "brand", unless = "#result == null")
    public Brand findById(Long id) {
        return r.findById(id).orElseThrow(() -> {
            log.error("Бренд с id={} отсутствует", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Бренд с id=%d отсутствует", id));
        });
    }
}
