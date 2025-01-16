package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SizeDTO {
    private Long id;
    private String valueName;
    private Integer sortOrder;

    public static SizeDTO fromSize(AttributeValue size){
        return SizeDTO.builder()
                .id(size.getId())
                .valueName(size.getValueName())
                .sortOrder(size.getSortOrder())
                .build();
    }
}
