package com.withinu.geo;

import com.withinu.config.WithinuProperties;
import com.withinu.entity.CampusBoundary;
import com.withinu.repository.CampusBoundaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampusBoundarySeeder implements ApplicationRunner {

    private final WithinuProperties props;
    private final CampusBoundaryRepository boundaryRepository;
    private final WKTReader wktReader = new WKTReader();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String wkt = props.geo().campusBoundaryWkt();
        if (wkt == null || wkt.isBlank()) {
            return;
        }
        Polygon polygon;
        try {
            polygon = (Polygon) wktReader.read(wkt);
        } catch (ParseException e) {
            throw new IllegalStateException(
                "CAMPUS_BOUNDARY_WKT is not a valid WKT polygon: " + wkt, e);
        }
        polygon.setSRID(4326);

        CampusBoundary boundary = boundaryRepository.findById(CampusBoundarySeed.ID)
            .orElseGet(() -> CampusBoundary.builder()
                .id(CampusBoundarySeed.ID)
                .name("Campus boundary (from CAMPUS_BOUNDARY_WKT)")
                .active(true)
                .createdAt(Instant.now())
                .build());
        boundary.setBoundary(polygon);
        boundary.setActive(true);
        boundary.setUpdatedAt(Instant.now());
        boundaryRepository.save(boundary);
        log.info("Campus boundary overridden from CAMPUS_BOUNDARY_WKT");
    }
}