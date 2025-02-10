package com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColorResponse {
    private Long id;
    private String valueName;
    private String valueImg;
    private Integer sortOrder;

    public static ColorResponse fromAttributeValues(AttributeValue color) {
        return ColorResponse.builder()
                .id(color.getId())
                .valueName(color.getValueName())
                .valueImg(color.getValueImg())
                .sortOrder(color.getSortOrder())
                .build();
    }
}
