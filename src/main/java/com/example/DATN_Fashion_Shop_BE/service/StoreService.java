package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.store.CreateStoreRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreInventoryResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreStockResponse;
import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public PageResponse<StoreResponse> searchStores(String name, String city, int page, int size, Double userLat, Double userLon) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Store> stores;
        if (name != null && !name.isEmpty() && city != null && !city.isEmpty()) {
            stores = storeRepository.findByNameContainingIgnoreCaseAndAddress_CityContainingIgnoreCase(name, city, pageable);
        } else if (name != null && !name.isEmpty()) {
            stores = storeRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (city != null && !city.isEmpty()) {
            stores = storeRepository.findByAddress_CityContainingIgnoreCase(city, pageable);
        } else {
            stores = storeRepository.findAll(pageable);
        }

        List<StoreResponse> storeResponses = stores.stream().map(store -> {
                    Double distance = (userLat != null && userLon != null) ?
                            calculateDistance(userLat, userLon,
                                    store.getAddress().getLatitude(), store.getAddress().getLongitude()) : null;
                    return StoreResponse.fromStoreDistance(store, distance);
                }).sorted(Comparator.comparing(StoreResponse::getDistance,
                        Comparator.nullsLast(Comparator.naturalOrder())))  // Sắp xếp theo distances
                .toList();

        return PageResponse.fromPage(new PageImpl<>(storeResponses, pageable, stores.getTotalElements()));
    }

    public StoreInventoryResponse stockInStore(
            Long productId,
            Long colorId,
            Long sizeId,
            Long storeId) {
        return  StoreInventoryResponse.builder()
                .quantityInStock(
                        inventoryRepository.findQuantityInStockStoreId(productId,colorId,sizeId,storeId)
                                .orElse(0))
                .build();
    }

    @Transactional
    public StoreResponse getStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        return StoreResponse.fromStore(store);
    }

    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        String fullAddress = request.getFull_address() != null && !request.getFull_address().isEmpty()
                ? request.getFull_address()
                : String.join(", ", request.getStreet(), request.getDistrict(), request.getWard(), request.getCity());

        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .ward(request.getWard())
                .district(request.getDistrict())
                .fullAddress(fullAddress)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        addressRepository.save(address);

        boolean isActive = request.getIsActive() != null && request.getIsActive();

        Store store = Store.builder()
                .name(request.getName())
                .phone(request.getPhoneNumber())
                .email(request.getEmail())
                .openHour(request.getOpenHour())
                .closeHour(request.getCloseHour())
                .isActive(isActive)
                .address(address)
                .build();
        storeRepository.save(store);

        return StoreResponse.fromStore(store);
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, CreateStoreRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        String fullAddress = request.getFull_address() != null && !request.getFull_address().isEmpty()
                ? request.getFull_address()
                : String.join(", ",
                request.getStreet(), request.getDistrict(), request.getWard(), request.getCity());

        Address address = store.getAddress();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setFullAddress(fullAddress);
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        addressRepository.save(address);

        boolean isActive = request.getIsActive() != null && request.getIsActive();

        store.setName(request.getName());
        store.setPhone(request.getPhoneNumber());
        store.setEmail(request.getEmail());
        store.setOpenHour(request.getOpenHour());
        store.setCloseHour(request.getCloseHour());
        store.setIsActive(isActive);
        storeRepository.save(store);

        return StoreResponse.fromStore(store);
    }

    @Transactional
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        Address address = store.getAddress();
        storeRepository.delete(store);

        if (!storeRepository.existsByAddress(address)) {
            addressRepository.delete(address);
        }
    }

    public Page<StoreStockResponse> getInventoryByStoreId(Long storeId, String languageCode, String productName, Long categoryId, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortBy);
        sort = sortDir.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Inventory> inventoryPage;
        List<Long> categoryIds = (categoryId != null) ? categoryRepository.findChildCategoryIds(categoryId) : new ArrayList<>();
        if (productName != null && categoryId != null) {
            inventoryPage = inventoryRepository.findByStoreIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCaseAndProductVariant_Product_Categories_IdIn(
                    storeId, languageCode, productName, categoryIds, pageable);
        } else if (categoryId != null) {
            inventoryPage = inventoryRepository.findByStoreIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Categories_IdIn(
                    storeId, languageCode, categoryIds, pageable);
        } else if (productName != null) {
            inventoryPage = inventoryRepository.findByStoreIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCase(
                    storeId, languageCode, productName, pageable);
        } else {
            inventoryPage = inventoryRepository.findByStoreIdAndProductVariant_Product_Translations_LanguageCode(
                    storeId, languageCode, pageable);
        }

        List<StoreStockResponse> stockResponses = inventoryPage.getContent()
                .stream()
                .map(inventory -> StoreStockResponse.fromInventory(inventory, languageCode))
                .collect(Collectors.toList());

        return new PageImpl<>(stockResponses, pageable, inventoryPage.getTotalElements());
    }


    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        final int EARTH_RADIUS = 6371;
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
