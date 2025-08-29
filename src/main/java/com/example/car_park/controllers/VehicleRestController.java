package com.example.car_park.controllers;

import com.example.car_park.controllers.dto.request.VehicleRequestDto;
import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.controllers.dto.response.VehicleResponseDto;
import com.example.car_park.dao.mapper.TripMapper;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.enums.Format;
import com.example.car_park.service.TripService;
import com.example.car_park.service.VehicleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class VehicleRestController {
    private final VehicleService vehicleService;
    private final TripService tripService;
    private final TripMapper tripMapper;

    @GetMapping({"", "/{id}"})
    public ResponseEntity<?> getVehicles(@AuthenticationPrincipal User user,
                                         @PathVariable(required = false) Long id,
                                         @PageableDefault(size = 5, sort = "price", direction = Sort.Direction.ASC) Pageable pageable) {
        if (id == null) {
            return ResponseEntity.ok(vehicleService.findAllForRest(user, pageable));
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

    @GetMapping("/{id}/trips_points")
    public ResponseEntity<?> getTripsByPoints(@AuthenticationPrincipal User user,
                                      @PathVariable Long id,
                                      @RequestParam ZonedDateTime begin,
                                      @RequestParam ZonedDateTime end,
                                      @RequestParam(defaultValue = "json", required = false) String format) {
        return tripService.getTripsByPointsForAPI(user, id, begin, end, Format.getByValue(format));
    }

    @GetMapping("/{id}/trips")
    public ResponseEntity<List<TripDto>> getTrips(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id,
                                                  @RequestParam ZonedDateTime begin,
                                                  @RequestParam ZonedDateTime end) {
        return ResponseEntity.ok(tripService.getTripsForAPI(user, id, begin, end));
    }
}
