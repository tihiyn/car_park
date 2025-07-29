package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.BrandDto;
import com.example.car_park.dao.BrandRepository;
import com.example.car_park.dao.mapper.BrandMapper;
import com.example.car_park.dao.model.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Бренд с id=%d отсутствует", id)));
    }

    public List<Brand> findByKeyword(String keyword) {
        return brandRepository.findByKeyword(keyword);
    }

    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    public List<BrandDto> findAllForRest() {
        return brandRepository.findAll().stream()
                .map(brandMapper::brandToBrandDto)
                .collect(Collectors.toList());
    }

    public void save(Brand brand) {
        brandRepository.save(brand);
    }

    public void delete(Long id) {
        brandRepository.deleteById(id);
    }
}