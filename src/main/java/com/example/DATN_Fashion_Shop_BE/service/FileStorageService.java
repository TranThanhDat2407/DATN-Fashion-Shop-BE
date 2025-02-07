package com.example.DATN_Fashion_Shop_BE.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public String uploadFileAndGetName(MultipartFile file, String subDirectory) {
        try {
            // Tạo tên file duy nhất
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Tạo đường dẫn thư mục
            Path directoryPath = Paths.get("uploads/" + subDirectory);
            Path filePath = directoryPath.resolve(fileName);

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Lưu file
            Files.write(filePath, file.getBytes());

            // Trả về chỉ tên file (nếu cần)
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }


    public void deleteFile(String fileUrl, String subDirectory) {
        try {
            // Xác định đường dẫn file đầy đủ
            Path filePath = Paths.get(BASE_UPLOAD_DIR + subDirectory).resolve(fileUrl).normalize();
            File file = filePath.toFile();

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Xóa file thành công: " + fileUrl);
                } else {
                    System.err.println("Không thể xóa file: " + fileUrl);
                }
            } else {
                System.err.println("File không tồn tại: " + fileUrl);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa file: " + e.getMessage());
        }
    }
}
