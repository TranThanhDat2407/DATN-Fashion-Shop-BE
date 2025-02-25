package com.example.DATN_Fashion_Shop_BE.dto.response.vnpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VnPayResponse {

    private String vnp_TmnCode;
    @JsonProperty("vnp_Amount")
    private String vnpAmount;
    @JsonProperty("vnp_BankCode")
    private String vnp_BankCode;
    @JsonProperty("vnp_BankTranNo")
    private String vnp_BankTranNo;
    @JsonProperty("vnp_CardType")
    private String vnp_CardType;
    @JsonProperty("vnp_OrderInfo")
    private String vnp_OrderInfo;
    @JsonProperty("vnp_PayDate")
    private String vnp_PayDate;
    @JsonProperty("vnp_ResponseCode")
    private String vnp_ResponseCode;
    @JsonProperty("vnp_TransactionNo")
    private String vnp_TransactionNo;
    @JsonProperty("vnp_TransactionStatus")
    private String vnp_TransactionStatus;
    @JsonProperty("vnp_SecureHash")
    private String vnp_SecureHash;



}
