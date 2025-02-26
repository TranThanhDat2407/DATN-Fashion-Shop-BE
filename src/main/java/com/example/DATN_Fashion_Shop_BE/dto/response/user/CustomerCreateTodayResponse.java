package com.example.DATN_Fashion_Shop_BE.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class CustomerCreateTodayResponse {
    Integer totalCustomerCreateToday ;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime customerCreateTodayDate;
}
