package com.example.car_park.controllers;

import com.example.car_park.dao.model.User;
import com.example.car_park.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class DriverRestController {
    private final DriverService driverService;

    // TODO: сделать контроллер для водителей с CRUD
    @GetMapping("")
    public ResponseEntity<?> getDrivers(@AuthenticationPrincipal User user,
                                        @PageableDefault(size = 5, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(driverService.findAllForRest(user, pageable));
    }

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> getDrivers(@AuthenticationPrincipal User user,
                                         @PathVariable(required = false) Long id,
                                         @PageableDefault(size = 5, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        if (id == null) {
            return ResponseEntity.ok(driverService.findAllForRest(user, pageable));
        }
        return ResponseEntity.ok(driverService.findByIdForRest(user, id));
    }
}
