package com.example.DATN_Fashion_Shop_BE.component;

import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.FieldErrorDetails;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final LocalizationUtils localizationUtils;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorDetails> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetails(
                        error.getField(),
                        error.getRejectedValue(),
                        localizationUtils.getLocalizedMessage(error.getDefaultMessage())  // Lấy thông điệp bản địa hóa từ khóa trong properties
                ))
                .collect(Collectors.toList());

        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_FAILED))  // Lấy thông điệp bản địa hóa cho lỗi tạo danh mục
                .errors(errors)
                .build();

        logger.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingPartException(MissingServletRequestPartException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(localizationUtils.getLocalizedMessage(MessageKeys.REQUIRED_PART_MISSING, ex.getRequestPartName()))  // Lấy thông điệp bản địa hóa cho thiếu phần yêu cầu
                .errors(List.of(new FieldErrorDetails(ex.getRequestPartName(),null, ex.getMessage())))
                .build();

        logger.warn("Missing request part: {}", ex.getRequestPartName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(localizationUtils.getLocalizedMessage(MessageKeys.UNEXPECTED_ERROR))  // Lấy thông điệp bản địa hóa cho lỗi không mong đợi
                .errors(List.of(new FieldErrorDetails("exception", null,ex.getMessage())))
                .build();

        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
