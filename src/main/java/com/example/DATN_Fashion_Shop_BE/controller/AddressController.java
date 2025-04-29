package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.AddressDTO;
import com.example.DATN_Fashion_Shop_BE.dto.ColorDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.address.AddressRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.service.AddressService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/address")
@AllArgsConstructor
public class AddressController {
    private final AddressService addressService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddressesByUserId(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ADDRESSES_RETRIEVED_SUCCESSFULLY),
                addresses
        ));
    }

    @GetMapping("/default/{userId}")
    public ResponseEntity<ApiResponse<AddressDTO>> getDefaultAddress(@PathVariable Long userId) {
        AddressDTO address = addressService.getDefaultAddressByUserId(userId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ADDRESSES_RETRIEVED_SUCCESSFULLY),
                address
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @RequestParam Long userId,
            @Valid @RequestBody AddressRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.ADDRESSES_ADD_FAILED),
                            localizationUtils
                    )
            );
        }
        AddressDTO newAddress = addressService.addNewAddress(userId, request);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ADDRESSES_ADD_SUCCESSFULLY),
                newAddress
        ));
    }

    @PutMapping("/update/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @RequestParam Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request,
            BindingResult bindingResult) {

        // Kiểm tra lỗi validation trước khi gọi service
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.ADDRESSES_UPDATE_FAILED),
                            localizationUtils
                    )
            );
        }

        // Gọi service để cập nhật địa chỉ
        AddressDTO updatedAddress = addressService.updateAddress(userId, addressId, request);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ADDRESSES_UPDATE_SUCCESSFULLY),
                updatedAddress
        ));
    }
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> deleteAddress(
            @RequestParam Long userId,
            @PathVariable Long addressId) {

        addressService.deleteAddress(userId, addressId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.ADDRESS_DELETE_SUCCESSFULLY),
                null
        ));
    }
    @PutMapping("/set-default")
    public ResponseEntity<ApiResponse<String>> setDefaultAddress(
            @RequestParam("addressId") Long addressId,
            @RequestParam("userId") Long userId) {

        // Gọi service để cập nhật địa chỉ mặc định
        boolean result = addressService.setDefaultAddress(addressId, userId);

        if (result) {

            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ADDRESS_SET_DEFAULT_SUCCESSFULLY),
                    null
            ));
        } else {

            return ResponseEntity.ok(ApiResponseUtils.successResponse(
                    localizationUtils.getLocalizedMessage(MessageKeys.ADDRESS_SET_DEFAULT_FAILED),
                    null
            ));
        }
    }


}
