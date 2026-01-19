package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.providers.DriverProvider;
import com.example.car_park.dao.model.User;
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
    private final DriverProvider dp;

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> find(@AuthenticationPrincipal User u,
                                  @PathVariable(required = false) Long id,
                                  @PageableDefault(size = 5, sort = "lastName", direction = Sort.Direction.ASC) Pageable p) {
        if (id == null) {
            return ResponseEntity.ok(dp.findAllForRest(u, p));
        }
        return ResponseEntity.ok(dp.findByIdForRest(u, id));
    }
}
