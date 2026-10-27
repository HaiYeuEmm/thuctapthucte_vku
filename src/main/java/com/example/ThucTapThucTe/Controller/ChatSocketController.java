package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.ChatMessageDto;
import com.example.ThucTapThucTe.Service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {
    private final ChatService service;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService service, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(ChatSendRequest request) {
        ChatMessageDto message = service.sendMessage(request.groupId(), request.senderId(), request.content());
        messagingTemplate.convertAndSend("/topic/groups/" + request.groupId(), message);
    }

    public record ChatSendRequest(Long groupId, Long senderId, String content) { }
}