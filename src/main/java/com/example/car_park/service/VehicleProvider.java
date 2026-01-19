package com.example.car_park.service;

import com.example.car_park.controllers.dto.response.VehicleInfoViewModel;
import com.example.car_park.dao.VehicleRepository;
import com.example.car_park.dao.mapper.VehicleMapper;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleProvider {
    private final VehicleService vs;
    private final VehicleRepository vr;
    private final VehicleMapper vm;
    private final ManagerService ms;

    public List<VehicleInfoViewModel> findAll(User user) {
        return vs.findAllByManager(ms.getManagerByUser(user)).stream()
            .map(vm::toModel)
            .toList();
    }

    public VehicleInfoViewModel findById(User user, Long id) {
        boolean isExist = vr.findById(id).isPresent();
        if (!isExist) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Транспортное средство с id=%d отсутствует", id));
        }
        Vehicle v = vs.getIfBelongs(ms.getManagerByUser(user), id);
        return vm.toModel(v);
    }
}
