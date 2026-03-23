package com.example.car_park.controllers.providers;

import com.example.car_park.controllers.dto.response.VehicleEditDto;
import com.example.car_park.dao.BrandCachedRepository;
import com.example.car_park.dao.BrandRepository;
import com.example.car_park.dao.mapper.BrandMapper;
import com.example.car_park.dao.model.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandProvider {
    private final BrandRepository r;
    private final BrandCachedRepository cr;
    private final BrandMapper m;

    public List<VehicleEditDto.BrandEditDto> findAll() {
        return r.findAll().stream()
            .map(m::toEditDto)
            .toList();
    }

    public Brand findById(Long id) {
        return cr.findById(id);
    }
}
