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
public class AttributeValuePatternResponse {
    private Long id;
    private String name;
    private String type;
    private Boolean isActive;

    public static AttributeValuePatternResponse fromAttributeValuePattern(AttributeValuePattern pattern) {
        return AttributeValuePatternResponse.builder()
                .id(pattern.getId())
                .name(pattern.getName())
                .type(pattern.getType())
                .isActive(pattern.getIsActive())
                .build();
    }
}
