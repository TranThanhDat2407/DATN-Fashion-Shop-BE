package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.StoreLoginDTO;
import com.example.DATN_Fashion_Shop_BE.dto.UserLoginDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.LoginResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.StaffResponse;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.Token;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.service.StaffService;
import com.example.DATN_Fashion_Shop_BE.service.TokenService;
import com.example.DATN_Fashion_Shop_BE.service.UserService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
            "or hasRole('ROLE_STAFF')" +
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> storeLogin(
            @Valid @RequestBody StoreLoginDTO storeLoginRequest,
            BindingResult result,
            HttpServletRequest request
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            result,
                            localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED),
                            localizationUtils
                    )
            );
        }

        try {
            String token = staffService.storeLogin(
                    storeLoginRequest.getEmail(),
                    storeLoginRequest.getPassword(),
                    storeLoginRequest.getStoreId()
            );

            String userAgent = request.getHeader("User-Agent");
            User userDetail = userService.getUserDetailsFromToken(token);
            Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

            LoginResponse loginResponse = LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build();

            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY),
                            loginResponse
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/check-user-store")
    public ResponseEntity<Boolean> checkUserInStore(
            @RequestParam Long userId,
            @RequestParam Long storeId) {

        boolean isInStore = staffService.isUserInStore(userId, storeId);
        return ResponseEntity.ok(isInStore);
    }

    @GetMapping("/list-staff")
    public ResponseEntity<ApiResponse<PageResponse<com.example.DATN_Fashion_Shop_BE.dto
            .response.staff.StaffResponse>>> getStaffList(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<com.example.DATN_Fashion_Shop_BE.dto.response.staff.StaffResponse>
                staffPage = staffService
                .getStaffList(storeId, id, name, startDate, endDate, roleId, sortBy, sortDir, page, size);

        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY),
                        PageResponse.fromPage(staffPage)
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<com.example.DATN_Fashion_Shop_BE.dto.response.staff.StaffResponse>>
    getStaffByUserId(@PathVariable Long userId) {
        try {
            Staff staff = staffService.getStaffByUserId(userId);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                            com.example.DATN_Fashion_Shop_BE.dto.response.staff.StaffResponse.fromStaff(staff)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.STAFF_NOT_FOUND),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        }
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<String>> updateStaffStatus(
            @PathVariable Long userId,
            @RequestParam boolean isActive) {
        try {
            staffService.updateStaffStatus(userId, isActive);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                            "Staff status updated successfully."
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.STAFF_NOT_FOUND),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        }
    }

    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        // Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
}
