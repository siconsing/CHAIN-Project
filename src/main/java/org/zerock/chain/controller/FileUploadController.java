// FileUploadController.java
package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Log4j2
@RestController
public class FileUploadController {

    private static final String UPLOAD_DIR = "C:/upload/";

    public FileUploadController() {
        // 생성자에서 업로드 디렉토리 존재 여부를 확인하고, 없으면 생성
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Upload directory created: {}", UPLOAD_DIR);
            }
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("projectFiles") MultipartFile file) {
        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + originalFileName;

            // 경로 조합 시 절대 경로 중복 방지
            Path path = Paths.get(UPLOAD_DIR, fileName).normalize();
            Files.write(path, file.getBytes());

            log.info("File uploaded successfully: {}", path.toString());
            return ResponseEntity.ok("/upload/" + fileName);

        } catch (IOException e) {
            log.error("File upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }


}