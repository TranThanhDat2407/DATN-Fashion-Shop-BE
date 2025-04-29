package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.request.store.CreateStoreRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.orderDetail.OrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreInventoryResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreOrderDetailResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreStockResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.*;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
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

    public Page<TopProductsInStoreResponse> getTopProductsInStore(
            Long storeId, String languageCode, Pageable pageable) {
        return orderDetailRepository.findTopProductsByStoreId(storeId, languageCode, pageable);
    }

    public Page<LatestOrderResponse> getLatestOrderDetails(Long storeId, String languageCode, Pageable pageable) {
        Page<OrderDetail> orderDetails =
                orderDetailRepository
                        .findLatestDoneOrderDetails
                                (storeId, pageable);

        return orderDetails.map(item -> LatestOrderResponse.fromOrderDetail(item,languageCode));
    }

    public List<StoreMonthlyRevenueResponse> getRevenueByMonth(Long storeId) {
        List<StoreMonthlyRevenueResponse> revenues = orderRepository.getMonthlyRevenueByStore(storeId);

        // Điền dữ liệu cho các tháng chưa có đơn hàng
        Map<Integer, Double> revenueMap = new HashMap<>();
        revenues.forEach(r -> revenueMap.put(r.getMonth(), r.getTotalRevenue()));

        List<StoreMonthlyRevenueResponse> fullRevenueList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            fullRevenueList.add(new StoreMonthlyRevenueResponse(i, revenueMap.getOrDefault(i, 0.0)));
        }
        return fullRevenueList;
    }

    public List<StoreWeeklyRevenueResponse> getWeeklyRevenue(Long storeId) {
        // Lấy ngày đầu và cuối tuần hiện tại
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY); // Bắt đầu từ thứ Hai
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY); // Kết thúc vào Chủ Nhật

        // Chuyển đổi sang LocalDateTime (thêm thời gian 00:00:00 và 23:59:59)
        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByStoreIdAndOrderStatus_StatusNameAndUpdatedAtBetween(
                storeId, "DONE", startDateTime, endDateTime);

        // Nhóm theo ngày trong tuần
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Order order : orders) {
            int dayOfWeek = order.getUpdatedAt().getDayOfWeek().getValue(); // 1 = Monday, ..., 7 = Sunday
            revenueMap.put(dayOfWeek, revenueMap.getOrDefault(dayOfWeek, 0.0) + order.getTotalPrice());
        }

        // Tạo danh sách đầy đủ 7 ngày (thứ Hai đến Chủ Nhật)
        List<StoreWeeklyRevenueResponse> weeklyRevenue = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            weeklyRevenue.add(new StoreWeeklyRevenueResponse(i, revenueMap.getOrDefault(i, 0.0)));
        }

        return weeklyRevenue;
    }

    public StoreOrderComparisonResponse getOrderComparison(Long storeId) {
        int currentYear = Year.now().getValue();
        long ordersWithUser = orderRepository.countByStoreIdAndUserIsNotNull(storeId, currentYear);
        long ordersWithoutUser = orderRepository.countByStoreIdAndUserIsNull(storeId, currentYear);

        return new StoreOrderComparisonResponse(ordersWithUser, ordersWithoutUser);
    }

    public StorePaymentComparisonResponse getOrderCountByPaymentMethod(Long storeId) {
        long cash = orderRepository.countByStoreIdAndPaymentMethod(storeId, 4L);
        long bankTransfer = orderRepository.countByStoreIdAndPaymentMethod(storeId, 3L);

        return new StorePaymentComparisonResponse(cash, bankTransfer);
    }

    public Long getTotalRevenueToday(Long storeId) {
        return orderRepository.getTotalRevenueToday(storeId);
    }

    public Long getTotalRevenueThisMonth(Long storeId) {
        return orderRepository.getTotalRevenueThisMonth(storeId);
    }

    public Long getTotalOrdersToday(Long storeId) {
        return orderRepository.getTotalOrdersToday(storeId);
    }

    public Long getTotalOrdersThisMonth(Long storeId) {
        return orderRepository.getTotalOrdersThisMonth(storeId);
    }

    public List<StoreRevenueByDateRangeResponse> getRevenueByDateRange(Long storeId, LocalDateTime startDate, LocalDateTime endDate) {
        // Lấy dữ liệu từ repository
        List<StoreRevenueByDateRangeResponse> revenueData = orderRepository
                .getRevenueByDateRange(storeId, startDate, endDate);

        // Tạo danh sách đầy đủ các tháng trong khoảng thời gian
        List<StoreRevenueByDateRangeResponse> fullRevenueData = new ArrayList<>();
        LocalDateTime currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            int month = currentDate.getMonthValue();
            int year = currentDate.getYear();

            // Kiểm tra xem tháng và năm có trong dữ liệu không
            StoreRevenueByDateRangeResponse existingData = revenueData.stream()
                    .filter(data -> data.getMonth() == month && data.getYear() == year)
                    .findFirst()
                    .orElse(null);

            if (existingData != null) {
                fullRevenueData.add(existingData); // Thêm dữ liệu thực tế
            } else {
                fullRevenueData.add(new StoreRevenueByDateRangeResponse(month, year, 0.0)); // Thêm dữ liệu mặc định
            }

            // Tăng thời gian lên 1 tháng
            currentDate = currentDate.plusMonths(1);
        }

        return fullRevenueData;
    }

    public List<StoreDailyRevenueResponse> getDailyRevenueByMonthAndYear(Long storeId, Integer month, Integer year) {
        // Lấy dữ liệu từ repository
        List<StoreDailyRevenueResponse> revenueData = orderRepository
                .getDailyRevenueByMonthAndYear(storeId, month, year);

        // Tạo danh sách đầy đủ các ngày trong tháng
        List<StoreDailyRevenueResponse> fullRevenueData = new ArrayList<>();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            int finalDay = day;
            // Kiểm tra xem ngày có trong dữ liệu không
            StoreDailyRevenueResponse existingData = revenueData.stream()
                    .filter(data -> data.getDay() == finalDay)
                    .findFirst()
                    .orElse(null);

            if (existingData != null) {
                fullRevenueData.add(existingData); // Thêm dữ liệu thực tế
            } else {
                fullRevenueData
                        .add(new StoreDailyRevenueResponse(
                                finalDay, month, year, 0.0)); // Thêm dữ liệu mặc định
            }
        }

        return fullRevenueData;
    }

    public List<TopStoresRevenueResponse> getTop5StoresByRevenue(int year) {
        List<TopStoresRevenueResponse> allStores = storeRepository.findAllStoresWithRevenueInYear(year);
        return allStores.stream()
                .sorted(Comparator.comparing(TopStoresRevenueResponse::getTotalRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<CityRevenueResponse> getRevenueStatisticsByCity(int year) {
        return storeRepository.getRevenueByCity(year);
    }
}
