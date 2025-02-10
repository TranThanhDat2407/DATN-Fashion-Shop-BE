package com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductMediaResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductTranslationResponse;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateColorResponse {
    private Long id;
    private String valueName;
    private String valueImg;
    private Integer sortOrder;
    private String attributeName;

    public static CreateColorResponse fromAttributeValues(AttributeValue color) {
        return CreateColorResponse.builder()
                .id(color.getId())
                .valueName(color.getValueName())
                .valueImg(color.getValueImg())
                .sortOrder(color.getSortOrder())
                .attributeName(color.getAttribute().getName())
                .build();
    }
}
