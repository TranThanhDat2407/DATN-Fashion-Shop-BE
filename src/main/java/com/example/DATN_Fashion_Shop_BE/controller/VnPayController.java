package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.dto.response.vnpay.VnPayResponse;
import com.example.DATN_Fashion_Shop_BE.service.OrderService;
import com.example.DATN_Fashion_Shop_BE.service.VNPayService;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@RestController
@RequestMapping("/api/v1/payment")
@AllArgsConstructor
public class VnPayController {
    private final VNPayService vnPayService;
    private static final Logger log = LoggerFactory.getLogger(VnPayController.class);
//    @GetMapping("/create-payment")
//    public String createPayment(@RequestParam long amount, @RequestParam String orderInfo, @RequestParam String transactionId ) {
//        String paymentUrl = VNPayService.createPaymentUrl(amount, orderInfo, transactionId, "127.0.0.1");
//        return paymentUrl;
//    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestParam Map<String, String> vnpParams)  {
        log.info("📥 Dữ liệu nhận từ frontend: {}", vnpParams);


        boolean isValid = vnPayService.verifyPayment(vnpParams);

        if (isValid) {

                log.info("✅ Giao dịch hợp lệ, cập nhật đơn hàng: ");
            return ResponseEntity.ok(Collections.singletonMap("valid", isValid));
        } else {
            log.warn("⚠ Giao dịch không hợp lệ: {}", vnpParams);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Giao dịch không hợp lệ"));

        }
    }

//    @GetMapping("/vnpay_ipn")
//    public ResponseEntity<?> vnpayIPN(@RequestBody VnPayResponse vnPayResponse){
//        log.info("📌 VNPay IPN nhận được: " + vnPayResponse);
//
//        boolean isValid = vnPayService.verifyPayment(vnPayResponse);
//
//        if (isValid) {
//            String orderId = vnPayResponse.get("vnp_TxnRef");
//            String transactionStatus = vnPayResponse.get("vnp_TransactionStatus");
//
//            if ("00".equals(transactionStatus)) {
//                System.out.println("✅ Đơn hàng " + orderId + " thanh toán thành công.");
//            } else {
//                System.out.println("❌ Đơn hàng " + orderId + " thất bại.");
//            }
//            return ResponseEntity.ok("Giao dịch hợp lệ");
//        } else {
//            System.out.println("⚠ Giao dịch không hợp lệ!");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao dịch không hợp lệ");
//        }
//    }


//    @PostMapping("/query")
//    public ResponseEntity<String> queryTransaction(
//            @RequestBody Map<String, String> requestData, HttpServletRequest request)
//            throws IOException {
//        String vnp_RequestId = VnPayConfig.getRandomNumber(8);
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "querydr";
//        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
//        String vnp_TxnRef = requestData.get("order_id");
//        String vnp_TransDate = requestData.get("trans_date");
//        String vnp_OrderInfo = "Kiem tra ket qua GD OrderId:" + vnp_TxnRef;
//
//        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss")
//                .format(Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7")).getTime());
//        String vnp_IpAddr = VnPayConfig.getIpAddress(request);
//
//        JsonObject vnp_Params = new JsonObject();
//        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
//        vnp_Params.addProperty("vnp_Version", vnp_Version);
//        vnp_Params.addProperty("vnp_Command", vnp_Command);
//        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);
//        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransDate);
//        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
//        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);
//
//        String hash_Data = String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode, vnp_TxnRef, vnp_TransDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
//        vnp_Params.addProperty("vnp_SecureHash", VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hash_Data));
//
//        return sendVNPayRequest(vnp_Params);
//    }
//
//    @PostMapping("/refund")
//    public ResponseEntity<String> refundTransaction(@RequestBody Map<String, String> requestData, HttpServletRequest request) throws IOException {
//        String vnp_RequestId = VnPayConfig.getRandomNumber(8);
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "refund";
//        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
//        String vnp_TxnRef = requestData.get("order_id");
//        String vnp_TransDate = requestData.get("trans_date");
//        String vnp_TransactionType = requestData.get("trantype");
//        String vnp_CreateBy = requestData.get("user");
//        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
//
//        long amount = Long.parseLong(requestData.get("amount")) * 100;
//        String vnp_Amount = String.valueOf(amount);
//        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7")).getTime());
//        String vnp_IpAddr = VnPayConfig.getIpAddress(request);
//
//        JsonObject vnp_Params = new JsonObject();
//        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
//        vnp_Params.addProperty("vnp_Version", vnp_Version);
//        vnp_Params.addProperty("vnp_Command", vnp_Command);
//        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
//        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
//        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);
//        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransDate);
//        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
//        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
//        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);
//
//        String hash_Data = String.join("|",
//                vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
//                vnp_TransactionType, vnp_TxnRef, vnp_Amount, "", vnp_TransDate,
//                vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);
//        vnp_Params.addProperty("vnp_SecureHash",
//                VnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hash_Data));
//
//        return sendVNPayRequest(vnp_Params);
//    }
//
//    private ResponseEntity<String> sendVNPayRequest(JsonObject vnp_Params) throws IOException {
//        URL url = new URL(vnPayConfig.getVnp_ApiUrl());
//        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Content-Type", "application/json");
//        con.setDoOutput(true);
//
//        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
//            wr.writeBytes(vnp_Params.toString());
//            wr.flush();
//        }
//
//        int responseCode = con.getResponseCode();
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String output;
//        while ((output = in.readLine()) != null) {
//            response.append(output);
//        }
//        in.close();
//
//        return ResponseEntity.status(responseCode).body(response.toString());
//    }
}
