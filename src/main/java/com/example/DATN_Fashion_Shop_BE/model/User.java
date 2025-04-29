package com.example.DATN_Fashion_Shop_BE.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class User extends BaseEntity implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String lastName;

    @Column(name = "phone", unique = true, length = 15)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "is_active",nullable = false)
    private Boolean isActive;

    @Column(name = "google_account_id", unique = true, length = 255)
    private String googleAccountId;

    @Column(name = "otp_request_time")
    private LocalDateTime otpRequestTime;

    @Column(name = "one_time_password", length = 10)
    private String oneTimePassword;

    @Column(name = "verify")
    private Boolean verify;

    @OneToMany(mappedBy = "user")
    @NotAudited
    private List<Notification> notification;


    @OneToMany(mappedBy = "user")
    @NotAudited
    Set<SecureToken> token;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user")
    @NotAudited
    private List<CouponUserRestriction> couponUserRestrictions;

    @OneToMany(mappedBy = "user")
    @NotAudited
    private List<UserAddress> userAddresses;
    //danh sách quyền (role)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_"+getRole().getName().toUpperCase()));
        return authorityList;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    //Trả về true nếu tài khoản chưa hết hạn.
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    //Trả về true nếu tài khoản chưa bị khóa.
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    //Trả về true nếu thông tin xác thực (mật khẩu) chưa hết hạn.
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    //Trả về true nếu tài khoản đang hoạt động.
    public boolean isEnabled() {
        return isActive;
    }
}
