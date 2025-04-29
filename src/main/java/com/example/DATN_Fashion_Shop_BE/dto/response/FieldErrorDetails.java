package com.example.DATN_Fashion_Shop_BE.dto.response;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorDetails {
    private String field;
    private Object rejectedValue;
    private String message;
}
