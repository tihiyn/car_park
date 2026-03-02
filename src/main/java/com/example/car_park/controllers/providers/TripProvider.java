package com.example.car_park.controllers.providers;

import com.example.car_park.api.AddressClient;
import com.example.car_park.controllers.dto.VehicleState;
import com.example.car_park.controllers.dto.response.GeoJsonFeatureCollection;
import com.example.car_park.controllers.dto.response.TripDto;
import com.example.car_park.controllers.dto.response.TripsViewModel;
import com.example.car_park.controllers.dto.response.VehicleLocationJsonDto;
import com.example.car_park.dao.TripRepository;
import com.example.car_park.dao.VehicleLocationRepository;
import com.example.car_park.dao.mapper.TripMapper;
import com.example.car_park.dao.mapper.VehicleLocationMapper;
import com.example.car_park.dao.model.Trip;
import com.example.car_park.dao.model.User;
import com.example.car_park.dao.model.Vehicle;
import com.example.car_park.dao.model.VehicleLocation;
import com.example.car_park.enums.Format;
import com.example.car_park.service.NotificationService;
import com.example.car_park.service.TripService;
import io.jenetics.jpx.GPX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TripProvider {
    private final TripService ts;
    private final NotificationService ns;
    private final TripRepository r;
    private final TripMapper m;
    private final VehicleProvider vp;
    private final AddressClient ac;
    private final VehicleLocationRepository vlr;
    private final VehicleLocationMapper vlm;

    public List<Trip> findInInterval(Vehicle v, ZonedDateTime s, ZonedDateTime b) {
        return r.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
            v,
            s.withZoneSameInstant(ZoneId.of("UTC")),
            b.withZoneSameInstant(ZoneId.of("UTC"))
        );
    }

    public List<TripsViewModel> findInIntervalForUI(User u, Long vId, ZonedDateTime s, ZonedDateTime b) {
        Vehicle v = vp.findById(u, vId);
        return findInInterval(v, s, b).stream()
            .map(t -> m.tripToTripsViewModel(
                t,
                ac.getAddressByCoords(t.getBeginLocation().getLocation().getX(), t.getBeginLocation().getLocation().getY()),
                ac.getAddressByCoords(t.getEndLocation().getLocation().getX(), t.getEndLocation().getLocation().getY()))
            )
            .toList();
    }

    public List<TripDto> findInIntervalForRest(User u, Long vId, ZonedDateTime s, ZonedDateTime b) {
        Vehicle v = vp.findById(u, vId);
        return findInInterval(v, s, b).stream()
            .map(t -> m.tripToTripDto(
                t,
                ac.getAddressByCoords(t.getBeginLocation().getLocation().getX(), t.getBeginLocation().getLocation().getY()),
                ac.getAddressByCoords(t.getEndLocation().getLocation().getX(), t.getEndLocation().getLocation().getY()),
                v.getEnterprise().getTimeZone())
            )
            .toList();
    }

    public Object findByPointsInInterval(User u, Long vId, ZonedDateTime s, ZonedDateTime b, Format f) {
        Vehicle v = vp.findById(u, vId);
        List<Trip> trips = r.findAllByVehicleAndBeginGreaterThanEqualAndEndLessThanEqual(
            v,
            s.withZoneSameInstant(ZoneId.of("UTC")),
            b.withZoneSameInstant(ZoneId.of("UTC"))
        );
        if (trips.isEmpty()) {
            return new ArrayList<>();
        }
        ZonedDateTime minBegin = ts.findMinBegin(trips);
        ZonedDateTime maxEnd = ts.findMaxEnd(trips);
        List<VehicleLocation> locs = vlr.findAllByVehicleAndTimestampBetween(v, minBegin, maxEnd);
        List<VehicleLocation> filtered = ts.filterByTripsBounds(locs, trips);
        if (Format.JSON == f) {
            return filtered.stream()
                .map(loc -> vlm.vehicleLocationToVehicleLocationJsonDto(loc, v.getEnterprise().getTimeZone()))
                .toList();
        }
        return vlm.vehicleLocationsToGeoJsonMap(filtered, v.getEnterprise().getTimeZone());
    }

    public void saveFromFile(User u, Long vId, MultipartFile f) {
        GPX gpx = getGPX(f);
        Vehicle v = vp.findById(u, vId);
        List<VehicleLocation> locs = ts.getLocationsFomGPX(gpx, v, r.findAllByVehicle(v));
        Trip t = saveNewTrip(v, locs);
        String start = ac.getAddressByCoords(t.getBeginLocation().getLocation().getX(), t.getBeginLocation().getLocation().getY());
        String finish = ac.getAddressByCoords(t.getEndLocation().getLocation().getX(), t.getEndLocation().getLocation().getY());
        ns.sendNotification(ts.buildNotification(v, start, finish)).subscribe();
    }

    private GPX getGPX(MultipartFile f) {
        try {
            Path tmp = Files.createTempFile("trip-", ".gpx");
            f.transferTo(tmp);
            GPX gpx = GPX.read(tmp);
            Files.deleteIfExists(tmp);
            return gpx;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить файл поездки");
        }
    }

    private Trip saveNewTrip(Vehicle v, List<VehicleLocation> locs) {
        VehicleLocation b = locs.getFirst();
        VehicleLocation e = locs.getLast();
        Trip newTrip = new Trip()
            .setBegin(b.getTimestamp())
            .setBeginLocation(b)
            .setEnd(e.getTimestamp())
            .setEndLocation(e)
            .setVehicle(v);
        vlr.saveAll(locs);
        return r.save(newTrip);
    }

    public List<GeoJsonFeatureCollection> findTripsForMap(List<Long> tIds) {
        Map<Long, List<VehicleLocation>> tMap = new HashMap<>();
        for (Long tId : tIds) {
            // FIXME: обращение к БД в цикле
            Trip t = r.findById(tId).orElse(null);
            if (t != null) {
                List<VehicleLocation> locs = vlr
                    .findAllByVehicleAndTimestampBetween(
                        t.getVehicle(),
                        t.getBegin(),
                        t.getEnd());
                tMap.put(tId, locs);
            }
        }
        return ts.convertToGeoJson(tMap);
    }

    public Flux<VehicleLocationJsonDto> streamLocation(Long vId) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double[] start = genRandomStartPoint();
        VehicleState s = new VehicleState(
            start[0],
            start[1],
            r.nextDouble() * 360,
            40 + r.nextDouble() * 40
        );
        return Flux.interval(Duration.ofSeconds(1))
            .map(tick -> {
                s.setBearing(s.getBearing() + (r.nextDouble() * 10 - 5));
                if (r.nextDouble() < 0.02) {
                    s.setBearing(s.getBearing() + 40 + r.nextDouble() * 50);
                }
                return ts.getNewPoint(s);
            });
    }
    private double[] genRandomStartPoint() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double latMin = 55.489;
        double latMax = 56.009;
        double lonMin = 37.319;
        double lonMax = 37.945;
        double lat = latMin + (latMax - latMin) * r.nextDouble();
        double lon = lonMin + (lonMax - lonMin) * r.nextDouble();
        return new double[]{lat, lon};
    }
}
