package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.EnterpriseDto;
import com.example.car_park.dao.EnterpriseRepository;
import com.example.car_park.dao.mapper.EnterpriseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseMapper enterpriseMapper;

    public List<EnterpriseDto> findAllEnterprises() {
        return enterpriseRepository.findAll().stream().map(enterpriseMapper::enterpriseToEnterpriseDto).collect(Collectors.toList());
    }
}
