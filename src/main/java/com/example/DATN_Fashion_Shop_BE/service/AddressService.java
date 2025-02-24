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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class AddressService {
    private final AddressRepository addressRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        // Lấy danh sách UserAddress theo userId
        List<UserAddress> userAddresses = userAddressRepository.findByUserId(userId);

        // Duyệt qua danh sách userAddresses để lấy thông tin địa chỉ và userAddress
        return userAddresses.stream()
                .map(userAddress -> {
                    Address address = userAddress.getAddress(); // Lấy địa chỉ từ UserAddress
                    return AddressDTO.fromAddress(address, userAddress); // Sử dụng method từ AddressDTO để map
                })
                .collect(Collectors.toList());
    }

    // lấy địa chỉ mặc định theo userId
    public AddressDTO getDefaultAddressByUserId(Long userId) {
        return userAddressRepository.findByUser_IdAndIsDefaultTrue(userId)
                .map(userAddress -> AddressDTO.fromAddress(userAddress.getAddress(), userAddress)) // Cung cấp cả Address và UserAddress
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ mặc định cho user ID: " + userId));
    }

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
                .city(request.getProvince()) // Chú ý: Đảm bảo bạn sử dụng đúng trường cho "tỉnh/thành phố"
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        // Lưu địa chỉ vào database
        Address savedAddress = addressRepository.save(newAddress);

        // Gán địa chỉ này vào user_address (có thể đặt mặc định nếu cần)
        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .address(savedAddress)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhoneNumber())
                .isDefault(false) // Đặt mặc định là false, có thể cập nhật sau
                .build();

        userAddressRepository.save(userAddress);

        // Trả về AddressDTO, bao gồm thông tin địa chỉ và thông tin từ UserAddress (tên, điện thoại)
        return AddressDTO.fromAddress(savedAddress, userAddress); // Trả về AddressDTO đầy đủ thông tin
    }

    // update address theo id user
    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressRequest request) {
        // Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Lấy thông tin địa chỉ từ bảng user_address
        UserAddress userAddress = userAddressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc về người dùng!"));

        // Cập nhật thông tin địa chỉ
        Address address = userAddress.getAddress();
        address.setStreet(request.getStreet());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setCity(request.getProvince()); // Đảm bảo bạn dùng đúng trường cho city/province
        address.setLatitude(Double.valueOf(request.getLatitude()));
        address.setLongitude(Double.valueOf(request.getLongitude()));

        // Lưu địa chỉ đã được cập nhật vào database
        Address updatedAddress = addressRepository.save(address);

        // Cập nhật thông tin người nhận trong bảng user_address
        userAddress.setFirstName(request.getFirstName());
        userAddress.setLastName(request.getLastName());
        userAddress.setPhone(request.getPhoneNumber());

        // Lưu thông tin người nhận đã được cập nhật
        userAddressRepository.save(userAddress);

        // Trả về AddressDTO, bao gồm cả thông tin địa chỉ và thông tin người nhận từ UserAddress
        return AddressDTO.fromAddress(updatedAddress, userAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        // Kiểm tra xem địa chỉ có thuộc về user không
        UserAddress userAddress = userAddressRepository.findByUserIdAndAddressId(userId, addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc về người dùng!"));

        // Lấy thông tin địa chỉ
        Address address = userAddress.getAddress();

        // Kiểm tra xem có phải địa chỉ mặc định của người dùng không
        if (userAddress.getIsDefault()) {
            throw new RuntimeException("Không thể xóa địa chỉ mặc định!");
        }

        // Xóa bản ghi trong bảng user_address
        userAddressRepository.delete(userAddress);

        // Kiểm tra xem có user nào khác đang dùng địa chỉ này không
        boolean isAddressUsedByOthers = userAddressRepository.existsByAddressId(addressId);

        // Nếu không còn ai sử dụng địa chỉ, thì xóa khỏi bảng address
        if (!isAddressUsedByOthers) {
            addressRepository.delete(address);
        }
    }
    public boolean setDefaultAddress(Long addressId, Long userId) {
        // Bước 1: Lấy tất cả các địa chỉ của người dùng và đặt isDefault = false
        List<UserAddress> userAddresses = userAddressRepository.findByUserId(userId);
        userAddresses.forEach(userAddress -> userAddress.setIsDefault(false));
        userAddressRepository.saveAll(userAddresses); // Lưu lại tất cả địa chỉ với isDefault = false

        // Bước 2: Tìm địa chỉ cần đặt làm mặc định và set isDefault = true
        Optional<UserAddress> userAddressOpt = userAddressRepository.findByUserIdAndAddressId(userId, addressId);
        if (userAddressOpt.isPresent()) {
            UserAddress userAddress = userAddressOpt.get();
            userAddress.setIsDefault(true); // Đặt địa chỉ này làm mặc định
            userAddressRepository.save(userAddress); // Lưu lại địa chỉ đã thay đổi
            return true; // Thành công
        }
        return false; // Không tìm thấy địa chỉ cần thay đổi
    }



}
