package org.zerock.chain.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class ImageUploadController {

    private static final String UPLOAD_DIR = Paths.get("src/main/resources/static/uploads/").toAbsolutePath().toString();

    @PostMapping("/upload_image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 파일 이름 생성 및 특수 문자 제거
        String originalFileName = file.getOriginalFilename();
        String safeFileName = UUID.randomUUID().toString() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

        // 파일 이름 길이 제한 (예: 최대 100자)
        if (safeFileName.length() > 100) {
            safeFileName = safeFileName.substring(0, 100);
        }

        // 파일 경로 설정
        File dest = new File(UPLOAD_DIR, safeFileName);

        try {
            // 디렉토리 존재 여부 확인 및 생성
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();  // 디렉토리 생성
            }

            // 파일 저장
            file.transferTo(dest);
            System.out.println("File saved: " + dest.getAbsolutePath());

            // Froala Editor에서 요구하는 형식으로 응답
            response.put("link", "/uploads/" + safeFileName);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            System.out.println("Error during file upload: " + e.getMessage());
            response.put("error", "Error during file upload.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}