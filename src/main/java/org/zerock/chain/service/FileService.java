package org.zerock.chain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service("yeongminFileService")
public class FileService {

    // 파일 크기 제한: 예를 들어 5MB로 설정
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    // 하드코딩된 파일 저장 경로
    private static final String uploadDir = "src/main/resources/static/uploads";

    public String saveFile(MultipartFile file) throws IOException {
        // 파일 크기 확인
        long fileSize = file.getSize();
        System.out.println("Uploaded file size: " + fileSize + " bytes");

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalStateException("파일 크기가 허용된 최대 크기를 초과하였습니다.");
        }

        // 파일 저장 경로 설정
        String directoryPath = uploadDir;
        Path directory = Paths.get(directoryPath);

        // 경로가 없으면 생성
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // UUID를 사용하여 파일 이름 생성
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuidFileName = UUID.randomUUID() + fileExtension;
        Path filePath = directory.resolve(uuidFileName);

        Files.copy(file.getInputStream(), filePath);

        // 원래 파일 이름과 UUID 이름을 함께 반환
        return originalFileName + "|" + filePath;
    }
}
