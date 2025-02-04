package com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateColorRequest {
    private String valueName;
    private Integer sortOrder;
}
