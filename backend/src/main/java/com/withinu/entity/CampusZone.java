package com.withinu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campus_zones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusZone {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(columnDefinition = "geometry(Polygon,4326)", nullable = false)
    private Polygon boundary;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}