package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.store.CreateStoreRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic.*;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.service.ExcelService;
import com.example.DATN_Fashion_Shop_BE.service.OrderService;
import com.example.DATN_Fashion_Shop_BE.service.StoreService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/store")
@AllArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final ExcelService excelService;
    private final LocalizationUtils localizationUtils;
    private final OrderService orderService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> searchStores(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon
            ) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.searchStores(name, city, page, size, userLat, userLon)
        ));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long storeId) {

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.getStoreById(storeId)
        ));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<StoreInventoryResponse>> storeInventory(
            @RequestParam Long productId,
            @RequestParam Long colorId,
            @RequestParam Long sizeId,
            @RequestParam Long storeId) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.stockInStore(productId, colorId, sizeId, storeId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@RequestBody CreateStoreRequest request) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.createStore(request)
        ));

    }

    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long storeId,
            @RequestBody CreateStoreRequest request) {
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                storeService.updateStore(storeId,request)
        ));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                null
        ));
    }


    @GetMapping("/product-inventory/{storeId}")
    public ResponseEntity<ApiResponse<PageResponse<StoreStockResponse>>> getInventoryByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "vi") String languageCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(storeService
                        .getInventoryByStoreId(storeId, languageCode, productName, categoryId, page, size, sortBy, sortDir))
        ));
    }

    @GetMapping("/dashboard/{storeId}/top-products")
    public ResponseEntity<ApiResponse<PageResponse<TopProductsInStoreResponse>>> getTopProducts(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "vi") String languageCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<TopProductsInStoreResponse> topProducts =
                storeService.getTopProductsInStore(storeId, languageCode, pageable);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(topProducts)
        ));
    }

    @GetMapping("/dashboard/{storeId}/latest-orders")
    public ResponseEntity<ApiResponse<PageResponse<LatestOrderResponse>>> getLatestOrders(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "vi") String languageCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LatestOrderResponse> latestOrders =
                storeService.getLatestOrderDetails(storeId, languageCode, pageable);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Orders retrieved successfully",
                PageResponse.fromPage(latestOrders)
        ));
    }

    @GetMapping("/dashboard/monthly-revenue")
    public ResponseEntity<List<StoreMonthlyRevenueResponse>> getMonthlyRevenue(@RequestParam Long storeId) {
        return ResponseEntity.ok(storeService.getRevenueByMonth(storeId));
    }

    @GetMapping("/dashboard/weekly-revenue")
    public ResponseEntity<List<StoreWeeklyRevenueResponse>> getWeeklyRevenue(@RequestParam Long storeId) {
        return ResponseEntity.ok(storeService.getWeeklyRevenue(storeId));
    }

    @GetMapping("dashboard/order-comparison/{storeId}")
    public ResponseEntity<StoreOrderComparisonResponse> getOrderComparison(@PathVariable Long storeId) {
        StoreOrderComparisonResponse response = storeService.getOrderComparison(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("dashboard/payment-comparison/{storeId}")
    public ResponseEntity<StorePaymentComparisonResponse> getPaymentComparison(@PathVariable Long storeId) {
        StorePaymentComparisonResponse response = storeService.getOrderCountByPaymentMethod(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("dashboard/revenue/today")
    public ResponseEntity<Long> getTotalRevenueToday(@RequestParam Long storeId) {
        return ResponseEntity.ok(storeService.getTotalRevenueToday(storeId));
    }

    @GetMapping("dashboard/revenue/month")
    public ResponseEntity<Long> getTotalRevenueThisMonth(@RequestParam Long storeId) {
        return ResponseEntity.ok(storeService.getTotalRevenueThisMonth(storeId));
    }

    @GetMapping("dashboard/orders/today")
    public ResponseEntity<Long> getTotalOrdersToday(@RequestParam Long storeId) {
        return ResponseEntity.ok(storeService.getTotalOrdersToday(storeId));
    }

    @GetMapping("dashboard/orders/month")
    public ResponseEntity<Long> getTotalOrdersThisMonth(@RequestParam Long storeId) {
        return ResponseEntity.ok(storeService.getTotalOrdersThisMonth(storeId));
    }

    @GetMapping("/revenue-by-date-range")
    public ResponseEntity<List<StoreRevenueByDateRangeResponse>> getRevenueByDateRange(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<StoreRevenueByDateRangeResponse> revenueData = storeService
                .getRevenueByDateRange(storeId, startDate, endDate);
        return ResponseEntity.ok(revenueData);
    }

    @GetMapping("/daily-revenue")
    public ResponseEntity<List<StoreDailyRevenueResponse>> getDailyRevenueByMonthAndYear(
            @RequestParam Long storeId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<StoreDailyRevenueResponse> revenueData = storeService
                .getDailyRevenueByMonthAndYear(storeId, month, year);
        return ResponseEntity.ok(revenueData);
    }

    @GetMapping("/export-revenue-by-date-range")
    public ResponseEntity<InputStreamResource> exportRevenueByDateRange(
            @RequestParam Long storeId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            // Tạo file Excel trong bộ nhớ
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelService.exportRevenueByDateRangeToExcel(storeId, start, end, outputStream);

            // Chuyển đổi thành InputStreamResource
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(inputStream);

            // Thiết lập headers để tải về file
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue_data.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/export-daily-revenue-by-month-and-year")
    public ResponseEntity<InputStreamResource> exportDailyRevenueByMonthAndYear(
            @RequestParam Long storeId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        try {
            // Tạo file Excel trong bộ nhớ
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelService.exportDailyRevenueByMonthAndYearToExcel(storeId, month, year, outputStream);

            // Chuyển đổi thành InputStreamResource
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(inputStream);

            // Thiết lập headers để tải về file
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue_data.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/export-store-orders")
    public ResponseEntity<InputStreamResource> exportStoreOrders(
            @RequestParam Long storeId,
            @RequestParam(required = false) Long orderStatusId,
            @RequestParam(required = false) Long paymentMethodId,
            @RequestParam(required = false) Long shippingMethodId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam String languageCode
    ) {
        try {
            // Lấy dữ liệu từ service
            List<StoreOrderResponse> storeOrders = orderService.getStoreOrdersByFilters(
                    storeId, orderStatusId, paymentMethodId, shippingMethodId, customerId, staffId, startDate, endDate, languageCode
            );
            storeOrders.sort((o1, o2) -> o2.getUpdatedAt().compareTo(o1.getUpdatedAt()));
            // Tạo file Excel trong bộ nhớ
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelService.exportStoreOrdersToExcel(storeOrders, outputStream);

            // Chuyển đổi thành InputStreamResource
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(inputStream);

            // Thiết lập headers để tải về file
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=store_orders.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/top-revenue")
    public ResponseEntity<List<TopStoresRevenueResponse>> getTop5StoresByRevenue(
            @RequestParam(required = false, defaultValue = "0") int year) {
        List<TopStoresRevenueResponse> result = storeService.getTop5StoresByRevenue(year);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/revenue-by-city")
    public ResponseEntity<List<CityRevenueResponse>> getRevenueByCity(
            @RequestParam(required = false, defaultValue = "0") int year) {

        List<CityRevenueResponse> result = storeService.getRevenueStatisticsByCity(year);
        return ResponseEntity.ok(result);
    }

}
