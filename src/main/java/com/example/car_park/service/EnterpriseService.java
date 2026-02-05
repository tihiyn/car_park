package com.example.car_park.service;

import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class EnterpriseService {
    private static final List<String> tzs = ZoneId.getAvailableZoneIds().stream().sorted().toList();

    public List<String> getTimeZones() {
        return tzs;
    }

    public Enterprise getIfBelongs(Manager m, Long id) {
        return m.getManagedEnterprises().stream()
            .filter(e -> e.getId().equals(id))
            .findAny()
            .orElseThrow(() -> new ResponseStatusException(FORBIDDEN,
                String.format("Предприятие с id=%d не относится к Вашим предприятиям", id)));
    }
}
