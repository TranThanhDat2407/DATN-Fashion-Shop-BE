package com.example.DATN_Fashion_Shop_BE.dto.response.audit;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.model.User;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

@Data
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryAudResponse extends AuditResponse{
    private Long id;
    private String imageUrl;   // Lấy image (hoặc imageUrl)
    private Boolean isActive;
    private Long parentId;     // ID của danh mục cha
    /**
     * Phương thức chuyển đổi từ entity Category và thông tin audit sang DTO.
     */
    public static CategoryAudResponse from(Category category,
                                           DefaultRevisionEntity revEntity,
                                           RevisionType revType) {

        return CategoryAudResponse.builder()
                .id(category.getId())
                .imageUrl(category.getImageUrl())
                .isActive(Boolean.TRUE.equals(category.getIsActive())) // Tránh NullPointerException
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null) // Lấy ID danh mục cha

                // Dữ liệu audit từ Envers
                .revision(revEntity.getId())
                .revisionType(revType.name())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .build();
    }
}
