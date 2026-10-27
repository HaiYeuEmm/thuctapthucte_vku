package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Dto.ChatMessageDto;
import com.example.ThucTapThucTe.Dto.ChatGroupDto;
import com.example.ThucTapThucTe.Entity.ChatGroup;
import com.example.ThucTapThucTe.Entity.ChatMessage;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.ChatGroupRepository;
import com.example.ThucTapThucTe.Repository.ChatMessageRepository;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatGroupRepository groupRepository;
    private final ChatMessageRepository messageRepository;
    private final usersservice usersService;
        private final TrainingClassService classService;
        private final RealtimeUpdatePublisher realtimePublisher;

    public ChatService(ChatGroupRepository groupRepository, ChatMessageRepository messageRepository,
            usersservice usersService, TrainingClassService classService, RealtimeUpdatePublisher realtimePublisher) {
        this.groupRepository = groupRepository;
        this.messageRepository = messageRepository;
        this.usersService = usersService;
        this.classService = classService;
        this.realtimePublisher = realtimePublisher;
    }

    public List<ChatGroup> findGroups(Long mentorId) {
        return mentorId == null ? groupRepository.findAll() : groupRepository.findByMentorId(mentorId);
    }

    public List<ChatGroup> findGroupsForUser(Long userId, UserRole role) {
        if (userId == null) {
            return groupRepository.findAll();
        }
        if (role == UserRole.MENTOR) {
            return groupRepository.findByMentorId(userId);
        }
        return groupRepository.findByMembersId(userId);
    }

    @Transactional
    public ChatGroup findOrCreateStudentGroup(Long studentId) {
        var student = usersService.requireRole(studentId, UserRole.STUDENT);
        var trainingClass = classService.findAll().stream()
                .filter(item -> item.getStudents().stream().anyMatch(member -> member.getId().equals(studentId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sinh vien chua duoc phan vao lop thuc tap"));
        if (trainingClass.getMentor() == null) {
            throw new IllegalArgumentException("Lop thuc tap chua duoc phan cong mentor");
        }

        ChatGroup group = groupRepository.findByTrainingClassId(trainingClass.getId()).orElse(null);
        if (group == null) {
            group = new ChatGroup("Trao doi - " + trainingClass.getName(), trainingClass.getMentor(), trainingClass);
        }
        group.addMember(student);
        return groupRepository.save(group);
    }

    @Transactional
    public ChatGroupDto findOrCreateMentorGroup(Long mentorId, Long classId) {
        var mentor = usersService.requireRole(mentorId, UserRole.MENTOR);
        var trainingClass = classService.findById(classId);
        if (trainingClass.getMentor() == null || !trainingClass.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("Mentor khong phu trach lop nay");
        }

        ChatGroup group = groupRepository.findByTrainingClassId(classId)
                .orElseGet(() -> new ChatGroup("Trao doi - " + trainingClass.getName(), mentor, trainingClass));
        group.addMember(mentor);
        trainingClass.getStudents().forEach(group::addMember);
        return ChatGroupDto.from(groupRepository.save(group));
    }

    public ChatGroup findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nhom chat voi ma " + groupId));
    }

    public ChatGroup createGroup(Long mentorId, Long classId, String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Ten nhom khong duoc de trong");
        var trainingClass = classService.findById(classId);
        var mentor = usersService.requireRole(mentorId, UserRole.MENTOR);
        if (trainingClass.getMentor() == null || !trainingClass.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("Mentor khong phu trach lop nay");
        }
        return groupRepository.save(new ChatGroup(name.trim(), mentor, trainingClass));
    }

    public ChatGroup addStudent(Long groupId, Long studentId) {
        ChatGroup group = findGroup(groupId);
        group.addMember(usersService.requireRole(studentId, UserRole.STUDENT));
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> findMessages(Long groupId, Long requesterId) {
        ChatGroup group = findGroup(groupId);
        requireMember(group, requesterId);
        return messageRepository.findTop50ByGroupIdOrderBySentAtAsc(groupId).stream().map(ChatMessageDto::from).toList();
    }

    @Transactional
    public ChatMessageDto sendMessage(Long groupId, Long senderId, String content) {
        ChatGroup group = findGroup(groupId);
        requireMember(group, senderId);
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Noi dung tin nhan khong duoc de trong");
        if (content.trim().length() > 2000) throw new IllegalArgumentException("Tin nhan khong duoc vuot qua 2000 ky tu");
        ChatMessageDto saved = ChatMessageDto.from(messageRepository.save(new ChatMessage(group,
            usersService.findById(senderId), content.trim())));
        realtimePublisher.publish("CHAT_MESSAGE", group.getTrainingClass().getId(), group.getId(), saved);
        return saved;
    }

    private void requireMember(ChatGroup group, Long userId) {
        if (userId == null || !group.hasMember(userId)) {
            throw new IllegalArgumentException("Nguoi dung khong phai thanh vien cua nhom chat");
        }
    }
}