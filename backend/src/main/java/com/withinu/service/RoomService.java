package com.withinu.service;

import com.withinu.dto.RoomResponse;
import com.withinu.entity.Room;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.mapper.RoomMapper;
import com.withinu.repository.MessageRepository;
import com.withinu.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;

    public List<RoomResponse> listActiveRooms() {
        return messageRepository.findActiveRoomsWithStats().stream()
            .map(RoomMapper::fromStatsRow)
            .toList();
    }

    public RoomResponse getRoom(UUID roomId) {
        Room room = roomRepository.findByActiveTrueAndId(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.ROOM_NOT_FOUND, "Room not found"));
        return RoomMapper.toResponse(room);
    }
}