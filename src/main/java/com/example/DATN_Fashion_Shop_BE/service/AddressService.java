package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.AddressDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.address.AddressRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.address.AddressReponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.model.UserAddress;
import com.example.DATN_Fashion_Shop_BE.repository.AddressRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserAddressRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AddressService {
    private final AddressRepository addressRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    // lấy danh sách địa chỉ theo userId
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        List<Address> addresses =  addressRepository.findAllByUserId(userId);
        return addresses.stream()
                .map(AddressDTO::fromAddress)
                .collect(Collectors.toList());
    }
    // lấy địa chỉ mặc định theo userId
    public AddressDTO getDefaultAddressByUserId(Long userId) {
        return userAddressRepository.findByUser_IdAndIsDefaultTrue(userId)
                .map(userAddress -> AddressDTO.fromAddress(userAddress.getAddress()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ mặc định cho user ID: " + userId));
    }
    // thêm địa chỉ mới theo userId
    @Transactional
    public AddressDTO addNewAddress(Long userId, AddressRequest request) {
        // Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Tạo địa chỉ mới
        Address newAddress = Address.builder()
                .street(request.getStreet())
                .district(request.getDistrict())
                .ward(request.getWard())
                .city(request.getProvince())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        // Lưu địa chỉ vào database
        Address savedAddress = addressRepository.save(newAddress);

        // Gán địa chỉ này vào user_address (không quan tâm mặc định)
        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .address(savedAddress)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhoneNumber())
                .isDefault(false) // Không đặt mặc định
                .build();

        userAddressRepository.save(userAddress);

        // Trả về AddressDTO
        return AddressDTO.fromAddress(savedAddress);
    }

    // update address theo id user
    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressRequest request) {
        // Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Lấy thông tin địa chỉ từ user_address
        UserAddress userAddress = userAddressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc về người dùng!"));

        // Cập nhật thông tin địa chỉ
        Address address = userAddress.getAddress();
        address.setStreet(request.getStreet());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setCity(request.getProvince());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        // Lưu địa chỉ cập nhật vào database
        Address updatedAddress = addressRepository.save(address);

        // Cập nhật thông tin trong bảng user_address
        userAddress.setFirstName(request.getFirstName());
        userAddress.setLastName(request.getLastName());
        userAddress.setPhone(request.getPhoneNumber());

        userAddressRepository.save(userAddress);

        // Trả về AddressDTO
        return AddressDTO.fromAddress(updatedAddress);
    }
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        // Kiểm tra xem địa chỉ có thuộc về user không
        UserAddress userAddress = userAddressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc về người dùng!"));

        // Lấy thông tin địa chỉ
        Address address = userAddress.getAddress();

        // Xóa bản ghi trong bảng user_address
        userAddressRepository.delete(userAddress);

        // Kiểm tra xem có user nào khác đang dùng địa chỉ này không
        boolean isAddressUsedByOthers = userAddressRepository.existsByAddressId(addressId);

        // Nếu không còn ai sử dụng địa chỉ, thì xóa khỏi bảng address
        if (!isAddressUsedByOthers) {
            addressRepository.delete(address);
        }
    }

}
