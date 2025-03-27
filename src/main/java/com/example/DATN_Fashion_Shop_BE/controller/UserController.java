package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.TotalOrderCancelTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.CustomerCreateTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserAdminResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.exception.ExpiredTokenException;
import com.example.DATN_Fashion_Shop_BE.exception.InvalidPasswordException;
import com.example.DATN_Fashion_Shop_BE.model.Token;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.service.TokenService;

import com.example.DATN_Fashion_Shop_BE.service.UserService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LocalizationUtils localizationUtils;
    private final TokenService tokenService;

    @Operation(
            summary = "Đăng ký tài khoản người dùng",
            description = """
                        API này được sử dụng để đăng ký một tài khoản người dùng mới.
                    """,
            tags = {"User"}
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            result,
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_FAILED),
                            localizationUtils
                    )
            );
        }

        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH)
                    )
            );
        }

        try {
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY),
                            user
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Xác thực tài khoản người dùng",
            description = "API này được sử dụng để xác thực tài khoản người dùng thông qua token đăng ký.",
            tags = {"User"}
    )
    @GetMapping("/register/verify")
    public ResponseEntity<ApiResponse<?>> verifyUser(@RequestParam(required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.TOKEN_IS_EXPIRED),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.TOKEN_IS_EXPIRED)
                    )
            );
        }

        try {
            userService.verifyUser(token);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY),
                            null
                    )
            );
        } catch (ExpiredTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.TOKEN_IS_EXPIRED),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.TOKEN_IS_EXPIRED)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_FAILED),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_FAILED)
                    )
            );
        }
    }


    @Operation(
            summary = "Đăng nhập ",
            description = """
                        API này được sử dụng để đăng nhập.
                    """,
            tags = {"User"}
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
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
            String token = userService.login(
                    userLoginDTO.getEmail(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 2 : userLoginDTO.getRoleId()
            );

            String userAgent = request.getHeader("User-Agent");
            User userDetail = userService.getUserDetailsFromToken(token);

            if (!userDetail.getVerify()) {
                return ResponseEntity.badRequest().body(
                        ApiResponseUtils.generateValidationErrorResponse(
                                result,
                                localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED),
                                localizationUtils
                        )
                );
            }

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

    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO
    ) {
        try {
            User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
            return ResponseEntity.ok(LoginResponse.builder()
                    .message("Refresh token successfully")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .username(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                            .build()
            );
        }
    }
    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        // Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }


    @PostMapping("/details")
    @Operation(summary = "Lấy thông tin chi tiết người dùng",
            description = """
                        API này được sử dụng để lấy thông tin chi tiết của người dùng từ token Bearer.
                    
                    """,
            tags = {"User"},
            security = { @SecurityRequirement(name = "bearer-key") })
    public ResponseEntity<ApiResponse<UserResponse>> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);

            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                            UserResponse.fromUser(user)
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

    @Operation(
            summary = "Kiểm tra xem email có tồn tại không",
            description = "API này kiểm tra xem một email đã được đăng ký trong hệ thống hay chưa.",
            tags = {"User"}
    )
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExist(@RequestParam String email) {
        boolean exists = userService.checkIfEmailExists(email);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Kiểm tra xem số điện thoại có tồn tại không",
            description = "API này kiểm tra xem một số điện thoại đã được đăng ký trong hệ thống hay chưa.",
            tags = {"User"}
    )
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneExist(@RequestParam String phone) {
        boolean exists = userService.checkIfPhoneExists(phone);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Quên mật khẩu và gửi OTP",
            description = "API này cho phép người dùng yêu cầu gửi OTP để đặt lại mật khẩu khi quên mật khẩu.",
            tags = {"User"}
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestParam String email) {
        try {
            userService.generateAndSendOTP(email);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.OTP_SENT_SUCCESSFULLY),
                            null
                    )
            );
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.EMAIL_SEND_FAILED)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.OTP_SEND_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Xác thực OTP",
            description = "API này dùng để xác thực OTP đã gửi cho người dùng trong quá trình quên mật khẩu.",
            tags = {"User"}
    )
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        try {
            if (userService.verifyOtp(email, otp)) {
                return ResponseEntity.ok(
                        ApiResponseUtils.successResponse(
                                localizationUtils.getLocalizedMessage(MessageKeys.OTP_VERIFIED_SUCCESSFULLY),
                                null
                        )
                );
            } else {
                return ResponseEntity.badRequest().body(
                        ApiResponseUtils.errorResponse(
                                HttpStatus.BAD_REQUEST,
                                localizationUtils.getLocalizedMessage(MessageKeys.INVALID_OTP),
                                null,
                                null,
                                localizationUtils.getLocalizedMessage(MessageKeys.OTP_INVALID_OR_EXPIRED)
                        )
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.OTP_VERIFICATION_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Lấy danh sách tất cả người dùng",
            description = """
                        API này được sử dụng để lấy danh sách tất cả người dùng từ hệ thống. 
                        Chỉ có người dùng với vai trò `ROLE_ADMIN` mới có quyền truy cập API này.
                    
                        **Yêu cầu:** Người dùng phải có vai trò `ROLE_ADMIN` và truyền token Bearer hợp lệ trong header `Authorization`.
                    
                        **Phản hồi:**
                        - `message`: Thông báo trạng thái API.
                        - `data`: Danh sách thông tin người dùng (nếu thành công).
                    """,
            tags = {"User"},
            security = {@SecurityRequirement(name = "bearer-key")}
    )
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserAdminResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<UserAdminResponse> userDTOPage = userService
                .getUsersPage(page, size, email, firstName, lastName, phone,
                        gender, isActive, startDate, endDate, roleId, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.USER_LIST_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(userDTOPage)
        ));
    }

    @Operation(
            summary = "Cập nhật thông tin người dùng",
            description = "API này cho phép cập nhật thông tin của một người dùng.",
            tags = {"User"}
    )
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserDTO updateUserDTO,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            result,
                            localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_FAILED),
                            localizationUtils
                    )
            );
        }

        try {
            User updatedUser = userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_SUCCESSFULLY),
                            updatedUser
                    )
            );
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Đặt lại mật khẩu người dùng",
            description = "API này cho phép người dùng thay đổi mật khẩu của mình.",
            tags = {"User"}
    )
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword
    ) {
        try {
            userService.resetPassword(userId, newPassword);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.RESET_PASSWORD_SUCCESSFULLY),
                            null
                    )
            );
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.INVALID_PASSWORD),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.RESET_PASSWORD_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Đặt lại mật khẩu người dùng",
            description = "API này cho phép người dùng thay đổi mật khẩu của mình.",
            tags = {"User"}
    )
    @PostMapping("/reset-password-email/{email}")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @PathVariable String email,
            @RequestParam String newPassword
    ) {
        try {
            userService.resetPassword(email, newPassword);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.RESET_PASSWORD_SUCCESSFULLY),
                            null
                    )
            );
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.RESET_PASSWORD_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Khóa hoặc mở khóa người dùng",
            description = "API này cho phép khóa hoặc mở khóa tài khoản người dùng.",
            tags = {"User"}
    )
    @PatchMapping("/{userId}/block-enable")
    public ResponseEntity<ApiResponse<?>> blockOrEnableUser(
            @PathVariable Long userId
    ) {
        try {
            userService.blockOrEnable(userId);
            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY),
                            null
                    )
            );
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND),
                            null,
                            null,
                            e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Thay đổi mật khẩu người dùng",
            description = "API này cho phép người dùng thay đổi mật khẩu của mình.",
            tags = {"User"}
    )
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestParam Long id,
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            result,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_CHANGE_FAILED),
                            localizationUtils
                    )
            );
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getRetypePassword())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH)
                    )
            );
        }

        try {

            userService.changePassword(id, changePasswordDTO.getCurrentPassword(), changePasswordDTO.getNewPassword());

            return ResponseEntity.ok(
                    ApiResponseUtils.successResponse(
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_CHANGED_SUCCESSFULLY),
                            null
                    )
            );
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_CHANGE_FAILED)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            null,
                            null,
                            localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_CHANGE_FAILED)
                    )
            );
        }
    }

    @Operation(
            summary = "Kiểm tra User có isActive=true và có trong database hay không",
            tags = {"User"}
    )
    @GetMapping("/valid/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkIsValidUserId (@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_CHANGED_SUCCESSFULLY),
                        userService.isUserValid(userId)
                )
        );
    }

    @GetMapping("cutomerCreate/today")
    public ResponseEntity<ApiResponse<CustomerCreateTodayResponse>> getCreateCustomerToday() {
        CustomerCreateTodayResponse userTotal = userService.getCreateCustomerToday();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được getTotalOrderCancelToday",
                userTotal
        ));
    }

    @GetMapping("cutomerCreate/yesterday")
    public ResponseEntity<ApiResponse<Integer>> getCreateCustomerTodayYesterday() {
        Integer userYesterday = userService.getCreateCustomerTodayYesterday();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Đã lấy được getTotalOrderCancelYesterday ",
                userYesterday
        ));
    }


}
