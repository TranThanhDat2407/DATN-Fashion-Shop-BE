package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.BannerDTO;
import com.example.DATN_Fashion_Shop_BE.dto.PaymentMethodDTO;
import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.PaymentMethod;
import com.example.DATN_Fashion_Shop_BE.repository.PaymentMethodRespository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentMethodService {
    private PaymentMethodRespository paymentMethodRespository;
    public List<PaymentMethodDTO> getAllPaymentMethod() {
        List<PaymentMethod> paymentMethod;
        paymentMethod = paymentMethodRespository.findAll();
        return  paymentMethod.stream()
                .map(PaymentMethod -> PaymentMethodDTO.fromPaymentMethod(PaymentMethod))
                .collect(Collectors.toList());
    }
}
