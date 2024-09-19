package org.zerock.chain.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileDownloadController {

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) throws IOException {
        // 파일 경로와 원본 파일 이름을 분리
        String[] parts = filePath.split("\\|");
        String originalFileName = parts[0];
        String savedFilePath = parts[1];

        // 파일 리소스를 가져옴
        Path path = Paths.get(savedFilePath);
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() || resource.isReadable()) {
            // 파일명을 UTF-8로 인코딩
            String encodedFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);

            // 파일이 존재하고 읽을 수 있는 경우 다운로드를 위한 응답 생성
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } else {
            throw new RuntimeException("Could not read the file!");
        }
    }
}

