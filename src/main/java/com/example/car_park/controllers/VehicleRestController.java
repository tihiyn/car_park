package com.example.car_park.controllers;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/vehicles")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class VehicleRestController {
    private final VehicleService vehicleService;

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> getVehicles(@AuthenticationPrincipal User user,
                                         @PathVariable(required = false) Long id) {
        if (id == null) {
            return ResponseEntity.ok(vehicleService.findAllForRest(user));
        }

        return ResponseEntity.ok(vehicleService.findByIdForRest(user, id));
    }

    @PostMapping("/new")
    public ResponseEntity<?> createVehicle(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody VehicleRequestDto vehicleRequestDto) {
        Vehicle crearedVehicle = vehicleService.create(user, vehicleRequestDto);

        return ResponseEntity.created(URI.create("/api/vehicles/" + crearedVehicle.getId())).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> editVehicle(@AuthenticationPrincipal User user,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody VehicleRequestDto vehicleRequestDto) {

        VehicleResponseDto updatedVehicle = vehicleService.update(user, id, vehicleRequestDto);

        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@AuthenticationPrincipal User user,
                                           @PathVariable Long id) {
        vehicleService.delete(user, id);

        return ResponseEntity.noContent().build();
    }
}
