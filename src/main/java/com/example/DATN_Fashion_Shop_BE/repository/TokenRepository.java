package com.example.DATN_Fashion_Shop_BE.repository;


import com.example.DATN_Fashion_Shop_BE.model.Token;
import com.example.DATN_Fashion_Shop_BE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUser(User user);
    Token findByToken(String token);
    Token findByRefreshToken(String token);
}

