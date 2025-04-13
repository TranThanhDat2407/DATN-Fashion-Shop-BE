package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.revenue.CountWishList;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
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
    private final ReviewRepository reviewRepository;
    private final ProductMediaRepository productMediaRepository;


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
        // Lấy danh sách sản phẩm bán chạy
        List<Top10Products> topProducts = orderDetailRepository.findTopSellingProducts(languageCode);

        // Lấy ảnh của từng sản phẩm từ ProductMedia repository
        List<Top10Products> modifiedList = topProducts.stream()
                .map(p -> {
                    String imageUrl = productMediaRepository
                            .findFirstByProduct_IdAndColorValue_IdOrderByIdAsc(p.getProductId(), p.getColorValueId())
                            .map(ProductMedia::getMediaUrl)
                            .orElse(null);
                    p.setImageUrl(imageUrl); // Thêm URL ảnh vào đối tượng
                    return p;
                })
                .collect(Collectors.toList());

        // Tính toán phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), modifiedList.size());
        List<Top10Products> pagedList = modifiedList.subList(start, end);

        // Trả về PageImpl với phân trang
        return new PageImpl<>(pagedList, pageable, modifiedList.size());
    }


    /**
     * Thống kê lượt yêu thích sản phẩm
     */
    public Page<CountWishList> getSortedProductStats(
            String languageCode,
            Long productId,
            String productName,
            int page,
            int size) {


        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());


        return wishListItemRepository.getProductStats(languageCode, productId, productName, pageable);
    }


    /**
     * Thống kê hàng tồn kho theo StoreId
     */
    public Page<InventoryStatistics> getInventoryStatistics(Long storeId, String productName, String color, String size, int page, int sizePerPage) {
        Pageable pageable = PageRequest.of(page, sizePerPage);
        return inventoryRepository.findInventoryByStore(storeId, productName, color, size, pageable);
    }


    public Page<CountReviews> getReviewStatistics(
            String languageCode,
            Long productId,
            String productName,
            Pageable pageable) {
        return reviewRepository.getProductReviewStatistics(languageCode,productId,productName, pageable);
    }

}

