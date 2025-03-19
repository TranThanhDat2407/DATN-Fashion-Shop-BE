package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.model.Order;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    public static Specification<Order> filterOrders(
            Long orderId,
            String status,
            String shippingAddress,
            Double minPrice,
            Double maxPrice,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            LocalDateTime updateFromDate,
            LocalDateTime updateToDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (orderId != null) {
                predicates.add(cb.equal(root.get("id"), orderId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("orderStatus").get("statusName"), status));
            }
            if (shippingAddress != null && !shippingAddress.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("shippingAddress")), "%" + shippingAddress.trim().toLowerCase() + "%"));
            }
            if (minPrice != null && maxPrice != null) {
                predicates.add(cb.between(root.get("totalPrice"), minPrice, maxPrice));
            }
            if (fromDate != null) {
                LocalDateTime toDateValue = (toDate != null) ? toDate : LocalDateTime.now();
                predicates.add(cb.between(root.get("createdAt"), fromDate, toDateValue));
            }

            if (updateFromDate != null) {
                LocalDateTime updateToDateValue = (updateToDate != null) ? updateToDate : LocalDateTime.now();
                predicates.add(cb.between(root.get("updatedAt"), updateFromDate, updateToDateValue));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
