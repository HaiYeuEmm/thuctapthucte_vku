package com.example.ThucTapThucTe.Controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DownloadController {

    private final Path uploadDir;

    public DownloadController(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/downloads/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws Exception {
        Path file = uploadDir.resolve(filename).normalize();
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }
}