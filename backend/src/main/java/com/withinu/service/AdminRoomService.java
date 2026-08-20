package com.withinu.service;

import com.withinu.dto.AdminRoomRequest;
import com.withinu.dto.RoomResponse;
import com.withinu.entity.Room;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.mapper.RoomMapper;
import com.withinu.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminRoomService {

    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<RoomResponse> listAllRooms() {
        return roomRepository.findAll().stream()
            .sorted((a, b) -> Boolean.compare(a.isActive(), b.isActive()))
            .map(RoomMapper::toResponse)
            .toList();
    }

    @Transactional
    public RoomResponse createRoom(AdminRoomRequest request) {
        String slug = slugOf(request);
        if (roomRepository.existsBySlug(slug)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Room slug already exists: " + slug);
        }
        Instant now = Instant.now();
        Room room = Room.builder()
            .name(request.name())
            .slug(slug)
            .description(request.description())
            .active(request.active() != null && request.active())
            .createdAt(now)
            .updatedAt(now)
            .build();
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse updateRoom(UUID roomId, AdminRoomRequest request) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.ROOM_NOT_FOUND, "Room not found"));
        String slug = slugOf(request);
        if (!room.getSlug().equals(slug) && roomRepository.existsBySlug(slug)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Room slug already exists: " + slug);
        }
        room.setName(request.name());
        room.setSlug(slug);
        room.setDescription(request.description());
        room.setActive(request.active() != null && request.active());
        room.setUpdatedAt(Instant.now());
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional
    public void deactivateRoom(UUID roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.ROOM_NOT_FOUND, "Room not found"));
        room.setActive(false);
        room.setUpdatedAt(Instant.now());
        roomRepository.save(room);
    }

    private String slugOf(AdminRoomRequest request) {
        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = request.name().toLowerCase().replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        }
        return slug;
    }
}