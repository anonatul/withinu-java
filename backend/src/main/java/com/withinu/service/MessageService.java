package com.withinu.service;

import com.withinu.dto.MessageRequest;
import com.withinu.dto.MessageResponse;
import com.withinu.dto.PageResponse;
import com.withinu.entity.AnonymousUser;
import com.withinu.entity.Message;
import com.withinu.entity.Room;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.mapper.MessageMapper;
import com.withinu.repository.AnonymousUserRepository;
import com.withinu.repository.MessageRepository;
import com.withinu.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    public static final int MAX_PAGE_SIZE = 50;

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final AnonymousUserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getMessages(UUID roomId, int page, int size, UUID currentUserId) {
        requireActiveRoom(roomId);
        int pageSize = Math.min(size, MAX_PAGE_SIZE);
        Page<Message> result = messageRepository.findByRoomIdOrderByCreatedAtDesc(
            roomId, PageRequest.of(Math.max(page, 0), pageSize));
        List<MessageResponse> content = result.getContent().stream()
            .map(m -> MessageMapper.toResponse(m, currentUserId))
            .toList();
        return new PageResponse<>(content, result.getNumber(), pageSize,
            result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, UUID userId) {
        Room room = requireActiveRoom(request.roomId());
        String content = normalizeWhitespace(request.content());
        if (content.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Message cannot be empty");
        }
        if (content.length() > 1000) {
            throw new ApiException(ErrorCode.MESSAGE_TOO_LONG, "Message cannot exceed 1000 characters");
        }
        AnonymousUser user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Unknown anonymous user"));

        Instant now = Instant.now();
        Message message = Message.builder()
            .room(room)
            .anonymousUser(user)
            .content(content)
            .deleted(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        return MessageMapper.toResponse(messageRepository.save(message), userId);
    }

    @Transactional
    public void softDeleteOwnMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));
        if (!message.ownedBy(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "You can only delete your own messages");
        }
        softDelete(message);
    }

    @Transactional
    public void softDeleteAnyMessage(UUID messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));
        softDelete(message);
    }

    private void softDelete(Message message) {
        Instant now = Instant.now();
        message.setDeleted(true);
        message.setDeletedAt(now);
        message.setUpdatedAt(now);
        message.setContent("");
    }

    private Room requireActiveRoom(UUID roomId) {
        return roomRepository.findByActiveTrueAndId(roomId)
            .orElseThrow(() -> new ApiException(ErrorCode.ROOM_NOT_FOUND, "Room not found"));
    }

    static String normalizeWhitespace(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }
}