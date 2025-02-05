package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name= "created_by")
    private Long createdBy;

    @Column(name= "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        Long currentUserId = getCurrentUserId();
        createdBy = currentUserId;
        updatedBy = currentUserId;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lấy ID người dùng hiện tại từ SecurityContextHolder.
     * Nếu không tìm thấy hoặc không đăng nhập, trả về null (hoặc bạn có thể trả về một giá trị mặc định).
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.example.DATN_Fashion_Shop_BE.model.User) {
                    return ((com.example.DATN_Fashion_Shop_BE.model.User) principal).getId();
                }
                // Nếu principal là kiểu String (ví dụ: username), bạn có thể thực hiện tìm kiếm ID từ username nếu cần
            }
        } catch (Exception e) {
            // Log exception nếu cần
        }
        return null;
    }
}
