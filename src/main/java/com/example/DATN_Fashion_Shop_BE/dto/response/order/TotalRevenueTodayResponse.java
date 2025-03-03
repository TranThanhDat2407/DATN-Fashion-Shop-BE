package com.example.DATN_Fashion_Shop_BE.dto.response.order;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TotalRevenueTodayResponse {
    Double totalRevenueToday;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime revenueTodayDate;
}
