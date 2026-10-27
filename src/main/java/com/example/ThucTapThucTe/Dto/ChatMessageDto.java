package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessageDto(Long id, Long groupId, Long senderId, String senderName,
        String content, LocalDateTime sentAt) {
    public static ChatMessageDto from(ChatMessage message) {
        return new ChatMessageDto(message.getId(), message.getGroup().getId(), message.getSender().getId(),
                message.getSender().getFullName(), message.getContent(), message.getSentAt());
    }
}