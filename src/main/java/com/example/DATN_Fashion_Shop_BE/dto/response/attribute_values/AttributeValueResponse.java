package com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValuePattern;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttributeValueResponse {
    private Long id;
    private String valueName;
    private String valueImg; // URL hoặc tên file
    private Integer sortOrder;

    public static AttributeValueResponse fromAttributeValue(AttributeValue attributeValue) {
        return AttributeValueResponse.builder()
                .id(attributeValue.getId())
                .valueName(attributeValue.getValueName())
                .valueImg(attributeValue.getValueImg())
                .sortOrder(attributeValue.getSortOrder())
                .build();
    }
}
