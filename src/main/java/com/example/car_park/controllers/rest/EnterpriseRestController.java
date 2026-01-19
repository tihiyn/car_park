package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.controllers.providers.EnterpriseProvider;
import com.example.car_park.dao.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/enterprises")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class EnterpriseRestController {
    private final EnterpriseProvider ep;

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> find(@AuthenticationPrincipal User u,
                                  @PathVariable(required = false) Long id,
                                  @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable p) {
        if (id == null) {
            return ResponseEntity.ok(ep.findAllForRest(u, p));
        }
        return ResponseEntity.ok(ep.findByIdForRest(u, id));
    }

    @PostMapping("/new")
    public ResponseEntity<?> create(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody EnterpriseRequestDto dto) {
        Long id = ep.create(user, dto);
        return ResponseEntity.created(URI.create("/api/enterprises/" + id)).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnterpriseResponseDto> edit(@AuthenticationPrincipal User u,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody EnterpriseRequestDto dto) {
        return ResponseEntity.ok(ep.edit(u, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User u,
                                    @PathVariable Long id) {
        ep.delete(u, id);
        return ResponseEntity.noContent().build();
    }
}
