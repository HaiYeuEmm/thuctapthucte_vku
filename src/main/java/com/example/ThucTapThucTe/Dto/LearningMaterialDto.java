package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.LearningMaterial;
import java.time.LocalDateTime;

public record LearningMaterialDto(Long id, String title, String description, String fileName,
        String fileType, String fileUrl, Long fileSize, LocalDateTime createdAt) {
    public static LearningMaterialDto from(LearningMaterial material) {
        return new LearningMaterialDto(
                material.getId(),
                material.getTitle(),
                material.getDescription(),
                material.getFileName(),
                material.getFileType(),
                material.getFileUrl(),
                material.getFileSize(),
                material.getCreatedAt()
        );
    }
}
