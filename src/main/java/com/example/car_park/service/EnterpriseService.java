package com.example.car_park.service;

import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.Manager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@Slf4j
public class EnterpriseService {
    private static final List<String> tzs = ZoneId.getAvailableZoneIds().stream().sorted().toList();

    public List<String> getTimeZones() {
        return tzs;
    }

    public Enterprise getIfBelongs(Manager m, Long id) {
        return m.getManagedEnterprises().stream()
            .filter(e -> e.getId().equals(id))
            .findAny()
            .orElseThrow(() -> {
                log.error("Предприятие с id={} не относится к предприятиям менеджера с id={}", id, m.getId());
                return new ResponseStatusException(FORBIDDEN,
                    String.format("Предприятие с id=%d не относится к Вашим предприятиям", id));
            });
    }
}
