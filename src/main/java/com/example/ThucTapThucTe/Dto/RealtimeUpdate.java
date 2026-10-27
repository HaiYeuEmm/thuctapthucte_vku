package com.example.ThucTapThucTe.Dto;

import java.time.LocalDateTime;

public record RealtimeUpdate(String type, Long classId, Long resourceId, Object data, LocalDateTime occurredAt) {
}