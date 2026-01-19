package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.controllers.providers.VehicleProvider;
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
@RequestMapping("/api/vehicles")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class VehicleRestController {
    private final VehicleProvider vp;

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> find(@AuthenticationPrincipal User u,
                                  @PathVariable(required = false) Long id,
                                  @PageableDefault(size = 5, sort = "price", direction = Sort.Direction.ASC) Pageable p) {
        if (id == null) {
            return ResponseEntity.ok(vp.findAllForRest(u, p));
        }
        return ResponseEntity.ok(vp.findByIdForRest(u, id));
    }

    @PostMapping("/new")
    public ResponseEntity<?> create(@AuthenticationPrincipal User u,
                                    @Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.created(URI.create("/api/vehicles/" + vp.create(u, dto))).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> edit(@AuthenticationPrincipal User u,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody VehicleRequestDto dto) {
        return ResponseEntity.ok(vp.edit(u, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user,
                                    @PathVariable Long id) {
        vp.delete(user, id);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/{id}/track")
//    public ResponseEntity<?> trackVehicle(@AuthenticationPrincipal User user,
//                                          @PathVariable Long id,
//                                          @RequestParam ZonedDateTime begin,
//                                          @RequestParam ZonedDateTime end,
//                                          @RequestParam(defaultValue = "json", required = false) String format) {
//        List<VehicleLocationJsonDto> vehicleLocationDtoList = vehicleService.getTrack(user, id, begin, end, format);
//        if ("geoJson".equalsIgnoreCase(format)) {
//            List<Map<String, Object>> features = new ArrayList<>();
//            for (VehicleLocationJsonDto loc : vehicleLocationDtoList) {
//                Point p = loc.getGeometry();
//                Map<String, Object> geometry = Map.of(
//                        "type", "Point",
//                        "coordinates", List.of(p.getX(), p.getY()) // X=долгота, Y=широта
//                );
//                Map<String, Object> properties = new LinkedHashMap<>();
//                properties.put("name", loc.getTimestamp());
//                properties.put("description", "Coordinates");
//                Map<String, Object> feature = Map.of(
//                        "type", "Feature",
//                        "geometry", geometry,
//                        "properties", properties
//                );
//                features.add(feature);
//            }
//            return ResponseEntity.ok(Map.of(
//                    "type", "FeatureCollection",
//                    "features", features
//            ));
//        }
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(vehicleLocationDtoList);
//    }
}
