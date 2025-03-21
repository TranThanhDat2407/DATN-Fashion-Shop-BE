package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.ProductVariantDetailDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.Notification.NotificationTranslationRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.promotion.PromotionRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionSimpleResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<PromotionSimpleResponse> getAllPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.getAllPromotions(pageable);

        return promotions.map(PromotionSimpleResponse::fromPromotion);
    }

    public PromotionResponse getActivePromotion() throws DataNotFoundException {
        Promotion promotion = promotionRepository.findByIsActiveTrue()
                .orElseThrow(() -> new DataNotFoundException("No active promotion found"));

        return PromotionResponse.fromPromotion(promotion);
    }

    // Lấy danh sách promotion đang active trong khoảng thời gian cho trước
    public Page<PromotionResponse> getActivePromotionsWithinDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findByDateRange(startDate, endDate, pageable);

        Page<PromotionResponse> responsePage = promotions.map(PromotionResponse::fromPromotion);

        return responsePage;
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .descriptions(request.getDescription())
                .discountPercentage(request.getDiscountRate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getStartDate().isBefore(LocalDateTime.now()) || request.getStartDate().isEqual(LocalDateTime.now()))
                .products(new HashSet<>())
                .build();

        // Lưu Promotion vào DB
        Promotion savedPromotion = promotionRepository.save(promotion);

        // Thêm Products vào Promotion
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            for (Product product : products) {
                product.setPromotion(savedPromotion);
                productRepository.save(product);
                savedPromotion.getProducts().add(product);
            }
        }

        return PromotionResponse.fromPromotion(savedPromotion);
    }

    @Transactional
    public PromotionResponse updatePromotion(Long promotionId, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        // Cập nhật các trường khác của Promotion
        promotion.setDescriptions(request.getDescription());
        promotion.setDiscountPercentage(request.getDiscountRate());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setIsActive(request.getStartDate().isBefore(LocalDateTime.now()) || request.getStartDate().isEqual(LocalDateTime.now()));

        // Cập nhật Promotion trong DB
        Promotion savedPromotion = promotionRepository.save(promotion);

        removeAllProductsFromPromotion(savedPromotion.getId());

        // Thêm hoặc cập nhật Products vào Promotion
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            for (Product product : products) {
                product.setPromotion(savedPromotion);
                productRepository.save(product);
            }
        }

        return PromotionResponse.fromPromotion(savedPromotion);
    }




    @Transactional
    public void deletePromotion(Long promotionId) {
        removeAllProductsFromPromotion(promotionId);
        
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        promotionRepository.delete(promotion);
    }


    @Transactional
//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")  // 00:00
    public void deactivateExpiredPromotions() {
        LocalDateTime now = LocalDateTime.now();

        List<Promotion> promotions = promotionRepository.findByEndDateBeforeAndIsActiveTrue(now);

        promotions.forEach(promotion -> {
            promotion.setIsActive(false);
            promotionRepository.save(promotion);
        });
    }

    @Transactional
    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")  // Chạy mỗi ngày lúc 00:00
    public void updatePromotions() {
        LocalDateTime now = LocalDateTime.now();

        // Kích hoạt khuyến mãi nếu đến ngày bắt đầu
        List<Promotion> promotionsToActivate = promotionRepository.findByStartDateBeforeAndEndDateAfter(now,now);
        for (Promotion promo : promotionsToActivate) {
            promo.setIsActive(true);
            if(promo.getIsActive()){
                // Lấy danh sách khách hàng để gửi thông báo
                List<User> customers = userRepository.findByRoleCustomer();
                // Tạo thông báo
                sendPromotionNotification(promo, customers);
            }
        }
        promotionRepository.saveAll(promotionsToActivate);


        // Vô hiệu hóa và xóa sản phẩm trong khuyến mãi nếu đến ngày kết thúc
        List<Promotion> promotionsToDeactivate = promotionRepository.findByEndDateBefore(now);
        for (Promotion promo : promotionsToDeactivate) {
            promo.setIsActive(false);

            // Cập nhật isActive của sản phẩm
            List<Product> products = productRepository.findByPromotion(promo);
            for (Product product : products) {
                product.setPromotion(null);
            }
            productRepository.saveAll(products);
        }
        promotionRepository.saveAll(promotionsToDeactivate);

    }

    @Transactional
    public PromotionResponse addProductsToPromotion(Long promotionId, List<Long> productIds) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        List<Product> products = productRepository.findAllById(productIds);

        for (Product product : products) {
            product.setPromotion(promotion);  // Gán Promotion cho Product
            productRepository.save(product);  // Lưu lại Product với Promotion
        }

        return PromotionResponse.fromPromotion(promotion);
    }

    @Transactional
    public void updateProductsPromotion(Long promotionId, List<Long> productIds, boolean activate) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        List<Product> products = productRepository.findAllById(productIds);

        for (Product product : products) {
            if (activate) {
                product.setPromotion(promotion);
            } else {
                product.setPromotion(null); // Gỡ bỏ promotion
            }
        }

        productRepository.saveAll(products);
    }

    private void sendPromotionNotification(Promotion promotion, List<User> users) {
        List<NotificationTranslationRequest> translations = notificationService.createPromotionTranslations(promotion);

        users.forEach(user -> {
            notificationService.createNotification(
                    user.getId(),
                    "PROMOTION",
                    null, // redirectUrl không cần backend xử lý
                    "promotion.jpg", // Không cần imageUrl
                    translations
            );
        });
    }

    @Transactional
//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    public void sendPromotionsBeforeStartDateForStaff() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1); // Ngày mai

        List<User> staff = userRepository.findByRoleStaff();

        Optional<Promotion> promotionOpt = promotionRepository.findPromotionBeforeStartDate(now, tomorrow);

        promotionOpt.ifPresent(promotion -> {
            sendPromotionForStaff(promotion, staff);
        });
    }

    private void sendPromotionForStaff(Promotion promotion, List<User> users) {
        List<NotificationTranslationRequest> translations = notificationService.createPromotionForStaff(promotion);

        users.forEach(user -> {
            notificationService.createNotification(
                    user.getId(),
                    "PROMOTION",
                    null, // redirectUrl không cần backend xử lý
                    "promotion.jpg", // Không cần imageUrl
                    translations
            );
        });
    }

    // Lấy danh sách productIds từ promotionId
    public List<Long> getProductIdsByPromotionId(Long promotionId) {
        return productRepository.findProductIdsByPromotionId(promotionId);
    }

    // Xóa sản phẩm khỏi promotion
    public void removeProductFromPromotion(Long promotionId, Long productId) {
        // Lấy promotion theo promotionId
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found with id: " + promotionId));

        // Lấy sản phẩm theo productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        // Kiểm tra xem sản phẩm có đang thuộc promotion này không
        if (product.getPromotion() != null && product.getPromotion().getId().equals(promotionId)) {
            // Xóa sản phẩm khỏi promotion
            product.setPromotion(null);
            productRepository.save(product); // Cập nhật lại sản phẩm trong DB
        } else {
            throw new IllegalStateException("Product is not assigned to this promotion.");
        }
    }

    @Transactional
    public void removeAllProductsFromPromotion(Long promotionId) {

        // Lấy tất cả các sản phẩm liên kết với Promotion (EAGER fetching hoặc lấy bằng JPQL)
        List<Product> products = productRepository.findProducByPromotionId(promotionId);

        // Xóa liên kết của các sản phẩm với Promotion
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                product.setPromotion(null); // Xóa liên kết promotion với product
                productRepository.save(product); // Cập nhật lại sản phẩm trong DB
            }
        }
    }

    public PromotionSimpleResponse getPromotionSimpleResponse(Long promotionId) {
        // Tìm Promotion theo promotionId
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        // Chuyển đổi Promotion thành PromotionSimpleResponse
        return PromotionSimpleResponse.fromPromotion(promotion);
    }
}
