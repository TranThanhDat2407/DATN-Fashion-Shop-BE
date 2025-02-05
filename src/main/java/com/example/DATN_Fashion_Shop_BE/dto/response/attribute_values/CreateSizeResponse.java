package com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSizeResponse {
    private Long id;
    private String valueName;
    private Integer sortOrder;
    private String attributeName;

    public static CreateSizeResponse fromAttributeValues(AttributeValue color) {
        return CreateSizeResponse.builder()
                .id(color.getId())
                .valueName(color.getValueName())
                .sortOrder(color.getSortOrder())
                .attributeName(color.getAttribute().getName())
                .build();
    }
}
