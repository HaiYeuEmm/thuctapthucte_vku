package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.ChatGroupDto;
import com.example.ThucTapThucTe.Dto.ChatMessageDto;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Service.ChatService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService service, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/groups")
    public List<ChatGroupDto> groups(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role) {
        UserRole resolvedRole = role == null ? null : UserRole.valueOf(role.trim().toUpperCase());
        return service.findGroupsForUser(userId, resolvedRole).stream().map(ChatGroupDto::from).toList();
    }

    @GetMapping("/student/{studentId}/group")
    public ChatGroupDto studentGroup(@PathVariable Long studentId) {
        return ChatGroupDto.from(service.findOrCreateStudentGroup(studentId));
    }

    @GetMapping("/mentor/{mentorId}/class/{classId}/group")
    public ChatGroupDto mentorGroup(@PathVariable Long mentorId, @PathVariable Long classId) {
        return service.findOrCreateMentorGroup(mentorId, classId);
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatGroupDto createGroup(@RequestBody CreateGroupRequest request) {
        return ChatGroupDto.from(service.createGroup(request.mentorId(), request.classId(), request.name()));
    }

    @PostMapping("/groups/{groupId}/members/{studentId}")
    public ChatGroupDto addStudent(@PathVariable Long groupId, @PathVariable Long studentId) {
        return ChatGroupDto.from(service.addStudent(groupId, studentId));
    }

    @PostMapping("/groups/{groupId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageDto sendMessage(@PathVariable Long groupId, @RequestBody SendMessageRequest request) {
        ChatMessageDto message = service.sendMessage(groupId, request.senderId(), request.content());
        messagingTemplate.convertAndSend("/topic/groups/" + groupId, message);
        return message;
    }

    @GetMapping("/groups/{groupId}/messages")
    public List<ChatMessageDto> messages(@PathVariable Long groupId, @RequestParam Long userId) {
        return service.findMessages(groupId, userId);
    }

    public record CreateGroupRequest(Long mentorId, Long classId, String name) { }
    public record SendMessageRequest(Long senderId, String content) { }
}