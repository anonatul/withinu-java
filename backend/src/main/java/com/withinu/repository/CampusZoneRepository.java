package com.withinu.repository;

import com.withinu.entity.CampusZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CampusZoneRepository extends JpaRepository<CampusZone, UUID> {
}