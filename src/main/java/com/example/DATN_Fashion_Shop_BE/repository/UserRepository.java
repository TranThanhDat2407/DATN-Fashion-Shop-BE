package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE (:email IS NULL OR u.email LIKE %:email%) " +
            "AND (:firstName IS NULL OR u.firstName LIKE %:firstName%) " +
            "AND (:lastName IS NULL OR u.lastName LIKE %:lastName%) " +
            "AND (:phone IS NULL OR u.phone LIKE %:phone%) " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND (:startDate IS NULL OR u.dateOfBirth >= :startDate) " +
            "AND (:endDate IS NULL OR u.dateOfBirth <= :endDate)" +
            "AND (:roleId IS NULL OR u.role.id = :roleId)")
    Page<User> findUsersByFilters(@Param("email") String email,
                                  @Param("firstName") String firstName,
                                  @Param("lastName") String lastName,
                                  @Param("phone") String phone,
                                  @Param("gender") String gender,
                                  @Param("isActive") Boolean isActive,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("roleId") Long roleId,
                                  Pageable pageable);
}
