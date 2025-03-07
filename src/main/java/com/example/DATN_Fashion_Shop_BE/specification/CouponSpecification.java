package com.example.DATN_Fashion_Shop_BE.specification;

import com.example.DATN_Fashion_Shop_BE.model.CouponUserRestriction;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponTranslation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CouponSpecification {
    public static Specification<Coupon> filterCoupons(String keyword, LocalDateTime expirationDate,
                                                      Float discountValue, Float minOrderValue,
                                                      String languageCode) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

//            // 🔹 JOIN với CouponTranslation để tìm theo name, description
//            Join<Coupon, CouponTranslation> translationJoin = root.join("translations", JoinType.LEFT);
//            if (userId != null) {
//                Join<Coupon, CouponUserRestriction> userJoin = root.join("userRestrictions", JoinType.LEFT);
//                predicates.add(criteriaBuilder.equal(userJoin.get("user").get("id"), userId));
//            }

            // 🔥 Tìm kiếm linh hoạt trên tất cả các tiêu chí
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        // 🔹 Tìm trong mã giảm giá
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),


                        // 🔹 Chuyển đổi discountValue, minOrderValue thành chuỗi rồi tìm kiếm
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.concat(criteriaBuilder.literal(""), root.get("discountValue"))), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.concat(criteriaBuilder.literal(""), root.get("minOrderValue"))), pattern),
                        // 🔹 Chuyển expirationDate thành chuỗi với định dạng chuẩn
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.concat(root.get("expirationDate"), "")), pattern)
                ));
            }


            // 🔹 Lọc theo ngày hết hạn (nếu có)
            if (expirationDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), expirationDate));
            }

            // 🔹 Lọc theo giá trị giảm giá
            if (discountValue != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountValue"), discountValue));
            }

            // 🔹 Lọc theo giá trị đơn hàng tối thiểu
            if (minOrderValue != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minOrderValue"), minOrderValue));
            }

            // 🔹 Lọc theo ngôn ngữ
//            if (languageCode != null && !languageCode.isEmpty()) {
//                predicates.add(criteriaBuilder.equal(translationJoin.get("language").get("code"), languageCode));
//            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}

