package com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SizeResponse {
    private Long id;
    private String valueName;
    private Integer sortOrder;

    public static SizeResponse fromAttributeValues(AttributeValue color) {
        return SizeResponse.builder()
                .id(color.getId())
                .valueName(color.getValueName())
                .sortOrder(color.getSortOrder())
                .build();
    }
}
