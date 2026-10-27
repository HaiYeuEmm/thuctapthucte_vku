package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Dto.RealtimeUpdate;
import java.time.LocalDateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeUpdatePublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeUpdatePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(String type, Long classId, Long resourceId, Object data) {
        if (classId != null) {
            messagingTemplate.convertAndSend("/topic/classes/" + classId,
                    new RealtimeUpdate(type, classId, resourceId, data, LocalDateTime.now()));
        }
    }
}