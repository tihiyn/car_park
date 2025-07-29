package com.example.car_park.controllers;

import com.example.car_park.controllers.dto.request.EnterpriseRequestDto;
import com.example.car_park.controllers.dto.response.EnterpriseResponseDto;
import com.example.car_park.dao.model.Enterprise;
import com.example.car_park.dao.model.User;
import com.example.car_park.service.EnterpriseService;
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
    private final EnterpriseService enterpriseService;

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> getEnterprises(@AuthenticationPrincipal User user,
                                            @PathVariable(required = false) Long id,
                                            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        if (id == null) {
            return ResponseEntity.ok(enterpriseService.findAllForRest(user, pageable));
        }
        return ResponseEntity.ok(enterpriseService.findByIdForRest(user, id));
    }

    @PostMapping("/new")
    public ResponseEntity<?> createEnterprise(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody EnterpriseRequestDto enterpriseRequestDto) {
        Enterprise createdEnterprise = enterpriseService.create(user, enterpriseRequestDto);
        return ResponseEntity.created(URI.create("/api/enterprises/" + createdEnterprise.getId())).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnterpriseResponseDto> editEnterprise(@AuthenticationPrincipal User user,
                                            @PathVariable Long id,
                                            @Valid @RequestBody EnterpriseRequestDto enterpriseRequestDto) {
        EnterpriseResponseDto updatedEnterprise = enterpriseService.update(user, id, enterpriseRequestDto);
        return ResponseEntity.ok(updatedEnterprise);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnterprise(@AuthenticationPrincipal User user,
                                 @PathVariable Long id) {
        enterpriseService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
