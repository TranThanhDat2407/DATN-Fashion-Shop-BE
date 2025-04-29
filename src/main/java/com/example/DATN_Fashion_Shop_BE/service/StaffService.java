package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.component.JwtTokenUtil;
import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.UpdateUserDTO;
import com.example.DATN_Fashion_Shop_BE.dto.UserDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.staff.StaffResponse;
import com.example.DATN_Fashion_Shop_BE.exception.*;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StaffService {
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

    public Staff getStaffByUserId(Long userId) throws Exception {
        return staffRepository.findByUserId(userId)
                .orElseThrow(() -> new Exception(
                        localizationUtils.getLocalizedMessage(MessageKeys.STAFF_NOT_FOUND)
                ));
    }

    public String storeLogin(String email, String password, Long storeId) throws Exception {
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

        Optional<Staff> optionalStaff = staffRepository.findByUserId(existingUser.getId());

        if (optionalStaff.isEmpty()) {
            throw new ForbiddenException("User is not a staff member");
        }

        Staff staff = optionalStaff.get();
        if (!staff.getStore().getId().equals(storeId)) {
            throw new ForbiddenException("Staff does not belong to this store");
        }


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

    public boolean isUserInStore(Long userId, Long storeId) {
        return staffRepository.existsByUser_IdAndStore_Id(userId, storeId);
    }


    public Page<StaffResponse> getStaffList(
            Long storeId, Long id, String name, LocalDateTime startDate, LocalDateTime endDate, Long roleId,
            String sortBy, String sortDir, int page, int size) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Staff> staffPage = staffRepository.findByFilters(storeId, id, name, startDate, endDate, roleId, pageable);

        List<StaffResponse> staffResponses = staffPage.getContent().stream()
                .map(StaffResponse::fromStaff)
                .collect(Collectors.toList());

        return new PageImpl<>(staffResponses, pageable, staffPage.getTotalElements());
    }

    public void updateStaffStatus(Long userId, boolean isActive) throws Exception {
        Staff staff = staffRepository.findByUserId(userId)
                .orElseThrow(() -> new Exception(
                        localizationUtils.getLocalizedMessage(MessageKeys.STAFF_NOT_FOUND)
                ));

        staff.getUser().setIsActive(isActive);
        userRepository.save(staff.getUser());
    }




}
