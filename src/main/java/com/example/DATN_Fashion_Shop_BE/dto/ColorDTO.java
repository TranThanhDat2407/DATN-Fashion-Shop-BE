package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Attribute;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColorDTO {
    private Long id;
    private String valueName;
    private String valueImg;
    private Integer sortOrder;

    public static ColorDTO fromColor(AttributeValue color){
        return ColorDTO.builder()
                .id(color.getId())
                .valueName(color.getValueName())
                .valueImg(color.getValueImg())
                .sortOrder(color.getSortOrder())
                .build();
    }
}
