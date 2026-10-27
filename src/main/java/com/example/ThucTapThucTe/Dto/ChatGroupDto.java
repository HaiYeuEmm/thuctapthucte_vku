package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.ChatGroup;
import java.util.List;

public record ChatGroupDto(Long id, String name, Long classId, String className, usersdto mentor, List<usersdto> members) {
    public static ChatGroupDto from(ChatGroup group) {
        return new ChatGroupDto(group.getId(), group.getName(), group.getTrainingClass().getId(),
            group.getTrainingClass().getName(), usersdto.from(group.getMentor()),
                group.getMembers().stream().map(usersdto::from).toList());
    }
}