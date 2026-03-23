package com.example.car_park.dao;

import com.example.car_park.dao.model.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
@RequiredArgsConstructor
public class BrandCachedRepository {
    private final BrandRepository r;

    @Cacheable(value = "brand", unless = "#result == null")
    public Brand findById(Long id) {
        return r.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format("Бренд с id=%d отсутствует", id)));
    }
}
