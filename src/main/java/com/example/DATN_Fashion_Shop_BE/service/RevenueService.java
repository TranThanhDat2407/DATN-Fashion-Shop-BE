package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountStartAndWishList;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.InventoryStatistics;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.Top10Products;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class RevenueService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final WishlistItemRepository wishListItemRepository;
    private final InventoryRepository inventoryRepository;
    /**
     * Lấy doanh thu theo ngày
     */
    public BigDecimal getRevenueByDay(LocalDate date) {
        return orderRepository.findTotalRevenueByDay(date).orElse(BigDecimal.ZERO);
    }


    /**
     * Lấy doanh thu theo tháng
     */
    public BigDecimal getRevenueByMonth(int year, int month) {
        return orderRepository.findTotalRevenueByMonth(year, month).orElse(BigDecimal.ZERO);
    }

    /**
     * Lấy doanh thu theo năm
     */
    public BigDecimal getRevenueByYear(int year) {
        return orderRepository.findTotalRevenueByYear(year).orElse(BigDecimal.ZERO);
    }

    /**
     * Lấy danh sách top 10 sản phẩm bán chạy nhất
     */

    public Page<Top10Products> getTopSellingProducts(String languageCode, Pageable pageable) {
        return orderDetailRepository.findTopSellingProducts(languageCode, pageable);
    }

    /**
     * Thống kê lượt yêu thích sản phẩm và đánh giá
     */
    public Page<CountStartAndWishList> getSortedProductStats(
            String languageCode,
            Long productId,
            String productName,
            Integer minStars,
            int page,
            int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(page, size);

        // Lấy dữ liệu từ repository
        Page<CountStartAndWishList> productStatsPage = wishListItemRepository.getProductStats(
                languageCode, productId, productName, minStars, pageable);

        List<CountStartAndWishList> productStats = new ArrayList<>(productStatsPage.getContent());

        // Sắp xếp theo tổng số wishlist & số review
        Comparator<CountStartAndWishList> comparator = Comparator
                .comparing(CountStartAndWishList::getTotalWishList, Comparator.reverseOrder())
                .thenComparing(CountStartAndWishList::getTotalStart, Comparator.reverseOrder());

        if ("reviews".equalsIgnoreCase(sortBy)) {
            comparator = Comparator
                    .comparing(CountStartAndWishList::getTotalStart, Comparator.reverseOrder())
                    .thenComparing(CountStartAndWishList::getTotalWishList, Comparator.reverseOrder());
        }

        productStats.sort(comparator);

        return new PageImpl<>(productStats, pageable, productStatsPage.getTotalElements());
    }





    /**
     * Thống kê hàng tồn kho theo StoreId
     */
    public Page<InventoryStatistics> getInventoryStatistics(Long storeId, String productName, String color, String size, int page, int sizePerPage) {
        Pageable pageable = PageRequest.of(page, sizePerPage);
        return inventoryRepository.findInventoryByStore(storeId, productName, color, size, pageable);
    }


}
