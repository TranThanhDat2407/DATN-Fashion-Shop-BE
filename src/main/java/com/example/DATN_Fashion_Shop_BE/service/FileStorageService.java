package com.example.DATN_Fashion_Shop_BE.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String BASE_UPLOAD_DIR = "uploads/images/";

    public String uploadFile(MultipartFile file, String subDirectory) {
        try {
            // Tạo đường dẫn lưu file trong thư mục con
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path directoryPath = Paths.get(BASE_UPLOAD_DIR + subDirectory);
            Path filePath = directoryPath.resolve(fileName);

            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(directoryPath);

            // Lưu file
            Files.write(filePath, file.getBytes());

            // Trả về đường dẫn URL tương đối
            return "/images/" + subDirectory + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
