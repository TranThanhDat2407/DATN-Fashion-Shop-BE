package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountWishList;
import com.example.DATN_Fashion_Shop_BE.dto.response.review.ReviewResponse;
import com.example.DATN_Fashion_Shop_BE.service.OrderService;
import com.example.DATN_Fashion_Shop_BE.service.RevenueService;
import com.example.DATN_Fashion_Shop_BE.service.ReviewService;
import com.example.DATN_Fashion_Shop_BE.service.WishlistService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/revenue")
@AllArgsConstructor
public class RevenueController {
    private final LocalizationUtils localizationUtils;
    private final OrderService orderService;
    private ReviewService reviewService;
    private final RevenueService revenueService;


    @GetMapping("/daily")
    public ResponseEntity<BigDecimal> getDailyRevenue(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(revenueService.getRevenueByDay(date));
    }

    @GetMapping("/monthly")
    public ResponseEntity<BigDecimal> getMonthlyRevenue(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(revenueService.getRevenueByMonth(year, month));
    }

    @GetMapping("/yearly")
    public ResponseEntity<BigDecimal> getYearlyRevenue(@RequestParam int year) {
        return ResponseEntity.ok(revenueService.getRevenueByYear(year));
    }

    @GetMapping("/top-10-products")
    public ResponseEntity<ApiResponse<Page<Top10Products>>> getTopSellingProducts(
            @RequestParam String languageCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Top10Products> pageResult = revenueService.getTopSellingProducts(languageCode, pageable);

        ApiResponse<Page<Top10Products>> response = ApiResponse.<Page<Top10Products>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Danh sách sản phẩm bán chạy lấy thành công")
                .data(pageResult)
                .errors(Collections.emptyList())
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/count/wishlist")
    public ResponseEntity<ApiResponse<Page<CountWishList>>> getProductStats(
            @RequestParam String languageCode,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Gọi service để lấy danh sách sản phẩm sắp xếp theo tổng số wishlist giảm dần
        Page<CountWishList> productStats = revenueService.getSortedProductStats(languageCode, productId, productName, page, size);

        // Tạo ApiResponse
        ApiResponse<Page<CountWishList>> response = ApiResponse.<Page<CountWishList>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(productStats)
                .build();

        return ResponseEntity.ok(response);
    }



    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<Page<InventoryStatistics>>> getInventory(
            @RequestParam Long storeId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String sizes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<InventoryStatistics> inventory = revenueService.getInventoryStatistics(storeId, productName, color, sizes, page, size);

        ApiResponse<Page<InventoryStatistics>> response = ApiResponse.<Page<InventoryStatistics>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(inventory)
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/count/reviews")
    public ResponseEntity<ApiResponse<Page<CountReviews>>> getReviewStatistics(
            @RequestParam String languageCode,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @PageableDefault(size = 10, sort = "totalReviews", direction = Sort.Direction.DESC)
            Pageable pageable) {


        Page<CountReviews> countReviews =revenueService.getReviewStatistics(languageCode,productId,productName, pageable);


        ApiResponse<Page<CountReviews>> response = ApiResponse.<Page<CountReviews>>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(countReviews)
                .build();

        return ResponseEntity.ok(response);


    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, page, size, sortBy, sortDir);

        PageResponse<ReviewResponse> pageResponse = PageResponse.fromPage(reviews);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                pageResponse));
    }

//    @GetMapping("/top-stores")
//    public ResponseEntity<ApiResponse<List<Top3Store>>> getTop3StoresByRevenue(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//
//        try {
//            List<Top3Store> topStores = orderService.getTop3StoresByRevenue(startDate, endDate);
//
//            ApiResponse<List<Top3Store>> response = ApiResponse.<List<Top3Store>>builder()
//                    .timestamp(LocalDateTime.now().toString())
//                    .status(HttpStatus.OK.value())
//                    .message("Lấy top 3 cửa hàng thành công")
//                    .data(topStores)
//                    .build();
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            ApiResponse<List<Top3Store>> errorResponse = ApiResponse.<List<Top3Store>>builder()
//                    .timestamp(LocalDateTime.now().toString())
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                    .message("Lỗi khi lấy top 3 cửa hàng")
//                    .data(null)
//                    .build();
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }
//    }

}
