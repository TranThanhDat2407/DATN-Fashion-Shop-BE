package com.example.DATN_Fashion_Shop_BE.utils;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.FieldErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ApiResponseUtils {

    /**
     * Tạo phản hồi thành công với dữ liệu.
     *
     * @param message Thông điệp phản hồi
     * @param data Dữ liệu phản hồi
     * @return ApiResponse chứa thông tin phản hồi
     */
    public static <T> ApiResponse<T> successResponse(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo phản hồi lỗi với danh sách lỗi.
     *
     * @param httpStatus Mã trạng thái HTTP
     * @param message Thông điệp phản hồi
     * @param errors Danh sách lỗi chi tiết
     * @return ApiResponse chứa thông tin lỗi
     */
    public static <T> ApiResponse<T> errorResponse(
            HttpStatus httpStatus,
            String message,
            List<FieldErrorDetails> errors) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(httpStatus.value())
                .message(message)
                .errors(errors)
                .build();
    }

    /**
     * Tạo phản hồi lỗi với một lỗi đơn lẻ.
     *
     * @param httpStatus Mã trạng thái HTTP
     * @param message Thông điệp phản hồi
     * @param field Lỗi thuộc tính
     * @param errorMessage Thông điệp lỗi
     * @return ApiResponse chứa thông tin lỗi
     */
    public static <T> ApiResponse<T> errorResponse(
            HttpStatus httpStatus,
            String message,
            String field,
            Object rejectedValue,
            String errorMessage) {
        return errorResponse(
                httpStatus,
                message,
                List.of(new FieldErrorDetails(field, rejectedValue, errorMessage))
        );
    }

    public static <T> ApiResponse<T> generateValidationErrorResponse(
            BindingResult bindingResult,
            String errorMessage,
            LocalizationUtils localizationUtils) {

        // Xử lý danh sách lỗi
        List<FieldErrorDetails> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorDetails(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null,
                        localizationUtils.getLocalizedMessage(error.getDefaultMessage())
                ))
                .collect(Collectors.toList());

        // Trả về phản hồi với lỗi
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .errors(fieldErrors)
                .data(null) // Dữ liệu trong trường hợp lỗi sẽ là null
                .build();
    }

}
