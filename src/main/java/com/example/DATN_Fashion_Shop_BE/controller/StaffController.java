package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.StaffResponse;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.service.StaffService;
import com.example.DATN_Fashion_Shop_BE.service.TokenService;
import com.example.DATN_Fashion_Shop_BE.service.UserService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/staff")
@RequiredArgsConstructor
public class StaffController {
    private final UserService userService;
    private final StaffService staffService;
    private final LocalizationUtils localizationUtils;
    private final TokenService tokenService;

    @PostMapping("/details")
    @PreAuthorize("hasRole('ROLE_ADMIN') " +
            "or hasRole('ROLE_STORE_STAFF')" +
            "or hasRole('ROLE_STORE_MANAGER')")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    public ResponseEntity<ApiResponse<StaffResponse>> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            Staff staff = staffService.getStaffByUserId(user.getId());

            StaffResponse staffResponse = StaffResponse.fromStaffAndUser(staff, user);

            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                            staffResponse
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_FAILED),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        }
    }
}
