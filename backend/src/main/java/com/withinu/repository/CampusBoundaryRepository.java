package com.withinu.repository;

import com.withinu.entity.CampusBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CampusBoundaryRepository extends JpaRepository<CampusBoundary, UUID> {

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM campus_boundaries cb
            WHERE cb.active = true
              AND ST_Contains(cb.boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
        )
        """, nativeQuery = true)
    boolean existsActiveBoundaryContaining(@Param("latitude") double latitude,
                                           @Param("longitude") double longitude);
}