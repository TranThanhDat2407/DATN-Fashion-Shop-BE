package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;

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
//        createdBy = getCurrentUserId(); // Lấy ID người dùng hiện tại
//        updatedBy = getCurrentUserId();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

//    private Long getCurrentUserId() {
//        // Giả sử bạn dùng Spring Security:
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()) {
//            return ((UserDetailsImpl) authentication.getPrincipal()).getId(); // Hoặc cách lấy ID người dùng phù hợp
//        }
//        return null; // Nếu không xác định được người dùng
//    }
}
