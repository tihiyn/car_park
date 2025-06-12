package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.EnterpriseDto;
import com.example.car_park.dao.mapper.EnterpriseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseService {
    private final EnterpriseMapper enterpriseMapper;
    private final ManagerService managerService;

    public List<EnterpriseDto> findAllEnterprises(User user) {
        return managerService.getManagerByUser(user).getManagedEnterprises().stream()
                .map(enterpriseMapper::enterpriseToEnterpriseDto)
                .toList();
    }
}
