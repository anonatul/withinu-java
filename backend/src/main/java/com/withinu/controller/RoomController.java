package com.withinu.controller;

import com.withinu.dto.RoomResponse;
import com.withinu.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Rooms", description = "Campus discussion rooms")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "List active rooms with message stats")
    @GetMapping
    public List<RoomResponse> listRooms() {
        return roomService.listActiveRooms();
    }

    @Operation(summary = "Get a single active room")
    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable UUID roomId) {
        return roomService.getRoom(roomId);
    }
}