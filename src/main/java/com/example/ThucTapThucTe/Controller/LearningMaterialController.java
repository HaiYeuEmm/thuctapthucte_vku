package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.LearningMaterialDto;
import com.example.ThucTapThucTe.Entity.LearningMaterial;
import com.example.ThucTapThucTe.Repository.LearningMaterialRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/materials")
public class LearningMaterialController {

    private final LearningMaterialRepository repository;
    private final Path uploadDir;

    public LearningMaterialController(LearningMaterialRepository repository,
            @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.repository = repository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi tạo thư mục tài liệu", e);
        }
    }

    @GetMapping
    public List<LearningMaterialDto> list() {
        return repository.findAll().stream().map(LearningMaterialDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LearningMaterialDto upload(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file) throws Exception {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên giáo trình.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file tài liệu.");
        }
        String originalName = file.getOriginalFilename() == null ? "material" : file.getOriginalFilename();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1) : "";
        String safeExtension = extension.matches("[A-Za-z0-9]{1,10}") ? "." + extension : "";
        String storedName = UUID.randomUUID() + safeExtension;
        Path target = uploadDir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        LearningMaterial material = new LearningMaterial(
                title.trim(),
                description == null ? "" : description.trim(),
                originalName,
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                "/downloads/" + storedName,
                file.getSize()
        );
        return LearningMaterialDto.from(repository.save(material));
    }
}