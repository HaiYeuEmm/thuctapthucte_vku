package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.ChatGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findByMentorId(Long mentorId);
    List<ChatGroup> findByMembersId(Long userId);
    java.util.Optional<ChatGroup> findByTrainingClassId(Long trainingClassId);
}