package com.withinu.controller.admin;

import com.withinu.dto.MessageResponse;
import com.withinu.dto.PageResponse;
import com.withinu.service.AdminMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin Messages", description = "Message moderation")
@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final AdminMessageService adminMessageService;

    @Operation(summary = "List all messages (paginated)")
    @GetMapping
    public PageResponse<MessageResponse> listMessages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "30") int size) {
        return adminMessageService.listAllMessages(page, size);
    }

    @Operation(summary = "Delete any message (soft delete)")
    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable UUID messageId) {
        adminMessageService.deleteMessage(messageId);
    }
}