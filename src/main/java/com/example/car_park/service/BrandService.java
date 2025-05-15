package com.example.car_park.service;

import com.example.car_park.dao.BrandRepository;
import com.example.car_park.dao.model.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public void save(Brand brand) {
        brandRepository.save(brand);
    }

    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    public Brand find(Long id) {
        return brandRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        brandRepository.deleteById(id);
    }

    public List<Brand> findByKeyword(String keyword) {
        return brandRepository.findByKeyword(keyword);
    }
}