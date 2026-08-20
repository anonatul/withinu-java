package com.withinu.controller;

import com.withinu.dto.MessageRequest;
import com.withinu.dto.MessageResponse;
import com.withinu.dto.PageResponse;
import com.withinu.security.AuthPrincipal;
import com.withinu.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Messages", description = "Anonymous room messages")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Get paginated messages for a room (newest first)")
    @GetMapping
    public PageResponse<MessageResponse> getMessages(
        @RequestParam UUID roomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "30") int size,
        @AuthenticationPrincipal AuthPrincipal principal) {
        return messageService.getMessages(roomId, page, size, principal.id());
    }

    @Operation(summary = "Send a message to a room")
    @PostMapping
    public MessageResponse sendMessage(@Valid @RequestBody MessageRequest request,
                                       @AuthenticationPrincipal AuthPrincipal principal) {
        return messageService.sendMessage(request, principal.id());
    }

    @Operation(summary = "Soft-delete your own message")
    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable UUID messageId,
                              @AuthenticationPrincipal AuthPrincipal principal) {
        messageService.softDeleteOwnMessage(messageId, principal.id());
    }
}