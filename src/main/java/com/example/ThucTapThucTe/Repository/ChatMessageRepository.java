package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50ByGroupIdOrderBySentAtAsc(Long groupId);
}