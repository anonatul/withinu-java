package com.withinu.controller.admin;

import com.withinu.dto.AdminRoomRequest;
import com.withinu.dto.RoomResponse;
import com.withinu.service.AdminRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin Rooms", description = "Room management")
@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final AdminRoomService adminRoomService;

    @Operation(summary = "List all rooms (including inactive)")
    @GetMapping
    public List<RoomResponse> listRooms() {
        return adminRoomService.listAllRooms();
    }

    @Operation(summary = "Create a room")
    @PostMapping
    public RoomResponse createRoom(@Valid @RequestBody AdminRoomRequest request) {
        return adminRoomService.createRoom(request);
    }

    @Operation(summary = "Update a room")
    @PatchMapping("/{roomId}")
    public RoomResponse updateRoom(@PathVariable UUID roomId,
                                   @Valid @RequestBody AdminRoomRequest request) {
        return adminRoomService.updateRoom(roomId, request);
    }

    @Operation(summary = "Deactivate a room")
    @DeleteMapping("/{roomId}")
    public void deactivateRoom(@PathVariable UUID roomId) {
        adminRoomService.deactivateRoom(roomId);
    }
}