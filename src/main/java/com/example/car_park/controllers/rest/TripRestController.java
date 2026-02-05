package com.example.car_park.controllers.rest;

import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.controllers.dto.response.VehicleLocationJsonDto;
import com.example.car_park.controllers.providers.TripProvider;
import com.example.car_park.dao.model.User;
import com.example.car_park.enums.Format;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripRestController {
    private final TripProvider tp;

    @PostMapping("/map")
    public ResponseEntity<?> findForMap(@RequestBody List<Long> tIds) {
        return ResponseEntity.ok(tp.findTripsForMap(tIds));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveFromFile(@AuthenticationPrincipal User u,
                                          @RequestParam("id") Long vId,
                                          @RequestParam("file") MultipartFile f) {
        try{
            tp.saveFromFile(u, vId, f);
            return ResponseEntity.ok("Поездка сохранена");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{vehicle_id}/trips_points")
    public ResponseEntity<?> findByPoints(@AuthenticationPrincipal User u,
                                          @PathVariable("vehicle_id") Long vId,
                                          @RequestParam("begin") ZonedDateTime s,
                                          @RequestParam("end") ZonedDateTime b,
                                          @RequestParam(defaultValue = "json", required = false) String format) {
        return ResponseEntity.ok(tp.findByPointsInInterval(u, vId, s, b, Format.getByValue(format)));
    }

    @GetMapping("/{vehicle_id}/trips")
    public ResponseEntity<List<TripDto>> find(@AuthenticationPrincipal User user,
                                              @PathVariable("vehicle_id") Long vId,
                                              @RequestParam("begin") ZonedDateTime s,
                                              @RequestParam("end") ZonedDateTime b) {
        return ResponseEntity.ok(tp.findInIntervalForRest(user, vId, s, b));
    }

    @GetMapping(value = "/{vehicle_id}/online", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VehicleLocationJsonDto> streamVehicleLocation(@PathVariable("vehicle_id") Long vId) {
        return tp.streamLocation(vId);
    }
}
