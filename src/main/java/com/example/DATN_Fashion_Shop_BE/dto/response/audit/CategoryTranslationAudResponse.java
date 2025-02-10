package com.example.DATN_Fashion_Shop_BE.dto.response.audit;
import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Category;
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
public class CategoryTranslationAudResponse extends AuditResponse{
    private Long id;
    private String name;
    private String languageCode;

    /**
     * Phương thức chuyển đổi từ entity Category và thông tin audit sang DTO.
     */
    public static CategoryTranslationAudResponse from(CategoriesTranslation categoriesTranslation,
                                                      DefaultRevisionEntity revEntity,
                                                      RevisionType revType) {
        return CategoryTranslationAudResponse.builder()
                // Lấy dữ liệu từ entity Category:
                .id(categoriesTranslation.getId())
                .name(categoriesTranslation.getName())
                .languageCode(categoriesTranslation.getLanguage().getCode())
                // Lấy dữ liệu audit từ đối tượng revision:
                .revision(revEntity.getId())
                .revisionType(revType.name())
                .createdAt(categoriesTranslation.getCreatedAt())
                .updatedAt(categoriesTranslation.getUpdatedAt())

                .build();
    }
}
