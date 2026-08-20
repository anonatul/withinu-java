package com.withinu.service;

import com.withinu.dto.MessageResponse;
import com.withinu.dto.PageResponse;
import com.withinu.entity.Message;
import com.withinu.mapper.MessageMapper;
import com.withinu.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMessageService {

    private final MessageRepository messageRepository;
    private final MessageService messageService;

    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> listAllMessages(int page, int size) {
        int pageSize = Math.min(size, 50);
        Page<Message> result = messageRepository.findAllByOrderByCreatedAtDesc(
            PageRequest.of(Math.max(page, 0), pageSize));
        List<MessageResponse> content = result.getContent().stream()
            .map(m -> MessageMapper.toResponse(m, UUID.randomUUID()))
            .toList();
        return new PageResponse<>(content, result.getNumber(), pageSize,
            result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }

    @Transactional
    public void deleteMessage(UUID messageId) {
        messageService.softDeleteAnyMessage(messageId);
    }
}