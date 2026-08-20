package com.withinu.mapper;

import com.withinu.dto.MessageResponse;
import com.withinu.entity.Message;

import java.util.UUID;

public final class MessageMapper {

    private MessageMapper() {
    }

    public static MessageResponse toResponse(Message message, UUID currentUserId) {
        return new MessageResponse(
            message.getId(),
            message.getRoom().getId(),
            displayName(message.getAnonymousUser().getId()),
            message.isDeleted() ? null : message.getContent(),
            message.isDeleted(),
            message.getAnonymousUser().getId().equals(currentUserId),
            message.getCreatedAt()
        );
    }

    public static String displayName(UUID anonymousUserId) {
        return "Anonymous #" + anonymousUserId.toString().substring(0, 4).toUpperCase();
    }
}