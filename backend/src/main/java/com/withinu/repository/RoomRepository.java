package com.withinu.repository;

import com.withinu.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByActiveTrueOrderByNameAsc();

    Optional<Room> findByActiveTrueAndId(UUID id);

    boolean existsBySlug(String slug);

    long countByActiveTrue();
}