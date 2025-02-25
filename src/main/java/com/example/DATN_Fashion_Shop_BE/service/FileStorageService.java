package com.example.DATN_Fashion_Shop_BE.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String BASE_UPLOAD_DIR = "uploads/images/";
    private final String BACKUP_DIR = "uploads/back_up/";

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

    public void backupAndDeleteFile(String fileUrl, String subDirectory) {
        try {
            Path sourcePath = Paths.get(BASE_UPLOAD_DIR + subDirectory).resolve(fileUrl).normalize();
            Path backupDirectory = Paths.get(BACKUP_DIR + subDirectory);
            Path backupPath = backupDirectory.resolve(fileUrl);

            Files.createDirectories(backupDirectory);

            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Backup file thành công: " + fileUrl);
            } else {
                System.err.println("File không tồn tại để backup: " + fileUrl);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi backup file: " + e.getMessage());
        }
    }

    public void restoreFile(String fileUrl, String subDirectory) {
        try {
            Path backupPath = Paths.get(BACKUP_DIR + subDirectory).resolve(fileUrl).normalize();
            Path restoreDirectory = Paths.get(BASE_UPLOAD_DIR + subDirectory);
            Path restorePath = restoreDirectory.resolve(fileUrl);

            Files.createDirectories(restoreDirectory);

            if (Files.exists(backupPath)) {
                Files.move(backupPath, restorePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Khôi phục file thành công: " + fileUrl);
            } else {
                System.err.println("File backup không tồn tại: " + fileUrl);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi khôi phục file: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // Chạy mỗi ngày lúc 2 giờ sáng
    public void cleanupOldBackups() {
        try {
            File backupFolder = new File(BACKUP_DIR);
            if (backupFolder.exists() && backupFolder.isDirectory()) {
                for (File file : backupFolder.listFiles()) {
                    Path filePath = file.toPath();
                    Instant lastModified = Files.getLastModifiedTime(filePath).toInstant();
                    // Xóa backup sau 30 ngày
                    int BACKUP_RETENTION_DAYS = 30;
                    Instant threshold = Instant.now().minus(BACKUP_RETENTION_DAYS, ChronoUnit.DAYS);

                    if (lastModified.isBefore(threshold)) {
                        Files.delete(filePath);
                        System.out.println("Xóa backup cũ: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa backup: " + e.getMessage());
        }
    }
}
