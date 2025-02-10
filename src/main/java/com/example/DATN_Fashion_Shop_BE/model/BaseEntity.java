package com.example.DATN_Fashion_Shop_BE.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Audited
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

        createdBy = getCurrentUserId();
        updatedBy = getCurrentUserId();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedBy = getCurrentUserId();
        updatedAt = LocalDateTime.now();
    }

    @PreRemove
    protected void onRemove() {
        updatedBy = getCurrentUserId();
        updatedAt = LocalDateTime.now();
    }


    /**
     * Lấy ID người dùng hiện tại từ SecurityContextHolder.
     * Nếu không tìm thấy hoặc không đăng nhập, trả về null (hoặc bạn có thể trả về một giá trị mặc định).
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.example.DATN_Fashion_Shop_BE.model.User user) {
                return user.getId();  // Trả về user ID chính xác
            }
        }
        return null;
    }
}
