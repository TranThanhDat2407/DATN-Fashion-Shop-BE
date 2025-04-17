package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.component.JwtTokenUtil;
import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.UpdateUserDTO;
import com.example.DATN_Fashion_Shop_BE.dto.UserDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.order.TotalOrderCancelTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.CustomerCreateTodayResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.user.UserAdminResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.exception.ExpiredTokenException;
import com.example.DATN_Fashion_Shop_BE.exception.InvalidPasswordException;
import com.example.DATN_Fashion_Shop_BE.exception.PermissionDenyException;
import com.example.DATN_Fashion_Shop_BE.mailing.AccountVerificationEmailContext;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.DATN_Fashion_Shop_BE.service.EmailService.log;


@RequiredArgsConstructor
@Service
public class UserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;
    private final CouponService couponService;
    private final SecureTokenService secureTokenService;
    private final CouponConfigService couponConfigService;
    private final HolidayCouponTranslationService holidayCouponTranslationService;


    public User createUser(UserDTO userDTO) throws Exception {

        // Lấy thông tin email và phone
        String email = userDTO.getEmail();
        String phone = userDTO.getPhone();

        // Kiểm tra xem email hoặc số điện thoại đã tồn tại
        if (userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException(
                    localizationUtils.getLocalizedMessage(MessageKeys.EMAIL_ALREADY_EXISTS));
        }

        if (userRepository.existsByPhone(phone)) {
            throw new DataIntegrityViolationException(
                    localizationUtils.getLocalizedMessage(MessageKeys.PHONE_ALREADY_EXISTS));
        }

        // Lấy thông tin role từ database
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));

        // Không cho phép đăng ký với vai trò ADMIN
        if (role.getName().equalsIgnoreCase(Role.ADMIN)) {
            throw new PermissionDenyException(
                    localizationUtils.getLocalizedMessage(MessageKeys.ADMIN_REGISTER_FORBIDDEN));
        }

        // Tạo mới User từ thông tin DTO
        User newUser = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .phone(userDTO.getPhone())
                .gender(userDTO.getGender())
                .email(userDTO.getEmail())
                .dateOfBirth(userDTO.getDateOfBirth())
                .googleAccountId(userDTO.getGoogleAccountId())
                .isActive(true)
                .verify(false)
                .build();

        // Thiết lập vai trò
        newUser.setRole(role);

        // Mã hóa mật khẩu nếu không có GoogleAccountId
        if (userDTO.getGoogleAccountId() == null) {
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }

        // Lưu thông tin người dùng vào database
        User savedUser = userRepository.save(newUser);


        // Nếu vai trò là Staff hoặc Store Manager, xử lý thêm thông tin Store
        if (role.getName().equalsIgnoreCase(Role.STORE_STAFF) ||
                role.getName().equalsIgnoreCase(Role.STORE_MANAGER)) {

            if (userDTO.getStoreId() == null) {
                throw new DataIntegrityViolationException(
                        localizationUtils.getLocalizedMessage(MessageKeys.STORE_ID_REQUIRED_FOR_ROLE));
            }

            // Lấy thông tin Store từ database
            Store store = storeRepository.findById(userDTO.getStoreId())
                    .orElseThrow(() -> new DataNotFoundException(
                            localizationUtils.getLocalizedMessage(MessageKeys.STORE_NOT_FOUND)));

            // Liên kết User với Store thông qua bảng Staff
            Staff staff = new Staff();
            staff.setUser(savedUser);
            staff.setStore(store);
            staffRepository.save(staff);
        }

        sendRegistrationConfirmationEmail(savedUser);

        // === Tạo mã giảm giá chào mừng thành viên mới ===
        CouponConfig config = couponConfigService.getCouponConfig("chaomungthanhvienmoi");
        if (config == null) {
            log.warn("⚠️ Không tìm thấy cấu hình mã giảm giá chào mừng! Bỏ qua việc tạo mã.");
        } else {
            // Lấy bản dịch từ DB
            List<CouponTranslationDTO> translations = holidayCouponTranslationService.getTranslationsByType("chaomungthanhvienmoi");
            // Nếu không có bản dịch, lấy bản dịch mặc định
            if (translations.isEmpty()) {
                log.warn("⚠️ Không tìm thấy bản dịch, sử dụng bản dịch mặc định.");
                translations = holidayCouponTranslationService.getTranslationsByType("chaomungthanhvienmoi");
            }
            // Tạo mã coupon dựa trên User ID và năm hiện tại
            LocalDate today = LocalDate.now();
            String couponCode = "WELCOME_" + savedUser.getId() + "_" + today.getYear();
            // Lấy ảnh từ config hoặc ảnh mặc định
            String imageUrl = (config.getImageUrl() != null) ? config.getImageUrl() : "/uploads/coupons/WelcomeCoupon.png";
            // Tạo mã giảm giá
            Coupon coupon = couponService.createCouponForUser(
                    couponCode,
                    config.getDiscountType(),
                    config.getDiscountValue(),
                    config.getMinOrderValue(),
                    config.getExpirationDays(),
                    savedUser,
                    imageUrl,
                    translations
            );
            log.info("✅ Đã tạo mã giảm giá chào mừng cho user {}: {}", savedUser.getEmail(), coupon.getCode());
        }


        return savedUser;
    }

    public void sendRegistrationConfirmationEmail(User user) throws MessagingException {

        SecureToken secureToken = secureTokenService.generateSecureToken(user);
        secureToken.setUser(user);
        secureTokenService.saveSecureToken(secureToken);

        AccountVerificationEmailContext context = new AccountVerificationEmailContext();
        context.init(user);
        context.setToken(secureToken.getToken());
        context.buildVerificationUrl("http://localhost:4200/client/vnd/vi" + "/verify-email", secureToken.getToken());

        String verificationUrl = context.getVerificationUrl(); // Lấy URL xác nhận từ context

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl);

    }

    public boolean verifyUser(String token) throws ExpiredTokenException {
        SecureToken secureToken = secureTokenService.findByToken(token);
        if (Objects.isNull(token) || !StringUtils.equals(token, secureToken.getToken()) || secureToken.isExpired()) {
            throw new ExpiredTokenException("Token is not valid");
        }

        User user = userRepository.getById(secureToken.getUser().getId());
        if (Objects.isNull(user)) {
            return false;
        }

        user.setVerify(true);
        userRepository.save(user);

        secureTokenService.removeToken(secureToken);
        return true;
    }


    public String login(String email, String password, Long roleId) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_EMAIL_PASSWORD));
        }
        //return optionalUser.get();//muốn trả JWT token ?
        User existingUser = optionalUser.get();
        //check password
        if (existingUser.getGoogleAccountId() == null) {
            if (!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new BadCredentialsException
                        (localizationUtils.getLocalizedMessage(MessageKeys.WRONG_EMAIL_PASSWORD));
            }
        }
        /*
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if(optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId())) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }
        */
        if (!optionalUser.get().getIsActive()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                email, password,
                existingUser.getAuthorities()
        );

        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserDetailsFromToken(String token) throws Exception {
        if (jwtTokenUtil.isTokenExpired(token)) {
            throw new ExpiredTokenException(
                    localizationUtils.getLocalizedMessage(MessageKeys.TOKEN_IS_EXPIRED)
            );
        }
        String email = jwtTokenUtil.extractEmail(token);
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception(localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND));
        }
    }

    public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        return getUserDetailsFromToken(existingToken.getToken());
    }


    public User updateUser(Long userId, UpdateUserDTO updateUserDTO) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND)
                ));

        existingUser.setFirstName(updateUserDTO.getFirstName());
        existingUser.setLastName(updateUserDTO.getLastName());
        existingUser.setPhone(updateUserDTO.getPhone());
        existingUser.setEmail(updateUserDTO.getEmail());
        existingUser.setDateOfBirth(updateUserDTO.getDateOfBirth());
        existingUser.setGender(updateUserDTO.getGender());
        existingUser.setIsActive(updateUserDTO.getIsActive());

        return userRepository.save(existingUser);
    }

    public void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND)
                ));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        //reset password => clear token
        List<Token> tokens = tokenRepository.findByUser(existingUser);
        tokenRepository.deleteAll(tokens);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND)
                ));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException(
                    localizationUtils.getLocalizedMessage(MessageKeys.INCORRECT_CURRENT_PASSWORD)
            );
        }

        if (currentPassword.equals(newPassword)) {
            throw new InvalidPasswordException(
                    localizationUtils.getLocalizedMessage(MessageKeys.NEW_PASSWORD_SAME_AS_OLD)
            );
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    public void blockOrEnable(Long userId) throws DataNotFoundException {
        // Tìm kiếm người dùng dựa trên userId
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND)
                ));

        // Cập nhật trạng thái isActive
        existingUser.setIsActive(!existingUser.getIsActive());

        // Lưu lại thay đổi vào cơ sở dữ liệu
        userRepository.save(existingUser);
    }

    public boolean checkIfEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkIfPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public void generateAndSendOTP(String email)  throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.EMAIL_NOT_FOUND)));

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000)); // Tạo OTP 6 chữ số
        user.setOneTimePassword(otp);
        user.setOtpRequestTime(LocalDateTime.now());
        userRepository.save(user);

        String subject = "<BRAND> Password Recovery Request";

        String message =
                "Thân gửi " + user.getLastName() + ",\n\n" +
                        "Mã OTP của bạn là: " + otp + ". Vui lòng nhập mã này để đặt lại mật khẩu.\n\n" +
                        "-----\n\n" +
                        "Dear " + user.getLastName() + ",\n\n" +
                        "Your OTP is: " + otp + ". Please enter this code to reset your password.";
        emailService.sendEmail(user.getEmail(), subject, message);
    }

    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.EMAIL_NOT_FOUND)
                ));

        // Kiểm tra xem OTP có đúng và chưa hết hạn + 5 phút
        if (user.getOneTimePassword().equals(otp) &&
                user.getOtpRequestTime().plusMinutes(5).isAfter(LocalDateTime.now())) {
            return true;
        }

        return false;
    }

    public void resetPassword(String email, String newPassword) throws DataNotFoundException , MessagingException{
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND)
                ));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        //reset password => clear token
        List<Token> tokens = tokenRepository.findByUser(existingUser);
        tokenRepository.deleteAll(tokens);

        String subject = "<BRAND> Password Change Confirmation";

        String message =
                "Thân gửi " + existingUser.getLastName() + ",\n\n" +
                        "Bạn đã đổi mật khẩu thành công!\n\n" +
                        "-----\n\n" +
                        "Dear " + existingUser.getLastName() + ",\n\n" +
                        "You have successfully changed your password!";
        emailService.sendEmail(existingUser.getEmail(), subject, message);
    }

    public Page<UserAdminResponse> getUsersPage(int page, int size, String email, String firstName, String lastName, String phone,
                                                String gender, Boolean isActive, LocalDateTime startDate, LocalDateTime endDate, Long roleId,
                                                String sortBy, String sortDir) {
        Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Validate and default the sortBy field
        String validSortBy = Arrays.asList("id", "email", "firstName", "lastName", "phone", "gender", "dateOfBirth", "isActive")
                .contains(sortBy) ? sortBy : "id"; // Default sort by ID

        // Build sorting
        Sort sort = Sort.by(direction, validSortBy);

        // Build pagination
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Fetch filtered and paginated users
        Page<User> userPage = userRepository
                .findUsersByFilters(email, firstName, lastName, phone, gender, isActive, startDate, endDate, roleId, pageRequest);

        // Map User to UserAdminResponse
        return userPage.map(UserAdminResponse::fromUser);
    }

    public int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }

        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(dateOfBirth, currentDate);
        return period.getYears();
    }

    public Boolean isUserValid (Long userId){
        return userRepository.existsByIdAndIsActiveTrue(userId);
    }

    public CustomerCreateTodayResponse getCreateCustomerToday() {
        List<User> totalUser = userRepository.getCreateCustomerToday();
        Integer count = totalUser.size();
        CustomerCreateTodayResponse response = new CustomerCreateTodayResponse();

        response.setTotalCustomerCreateToday(count);
        if (!totalUser.isEmpty()) {
            response.setCustomerCreateTodayDate(totalUser.get(0).getCreatedAt());
        }

        return response;
    }
    public Integer getCreateCustomerTodayYesterday() {
        List<User> totalUser = userRepository.getCreateCustomerTodayYesterday();
        Integer count = totalUser.size();

        return count;
    }




}
