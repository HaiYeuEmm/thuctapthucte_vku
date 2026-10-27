package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ChatGroup group;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    private String content;
    private LocalDateTime sentAt;

    protected ChatMessage() {
    }

    public ChatMessage(ChatGroup group, User sender, String content) {
        this.group = group;
        this.sender = sender;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public ChatGroup getGroup() { return group; }
    public User getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
}