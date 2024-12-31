package com.example.DATN_Fashion_Shop_BE.dto.response;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String timestamp;
    private int status;
    private String message;
    private T data;
    private List<FieldErrorDetails> errors;
}
