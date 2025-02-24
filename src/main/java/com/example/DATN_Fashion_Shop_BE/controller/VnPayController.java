package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.service.VNPayService;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;




@RestController
@RequestMapping("/api/v1/payment")
@AllArgsConstructor
public class VnPayController {
    private final VNPayService vnPayService;

    @GetMapping("/create-payment")
    public String createPayment(@RequestParam long amount, @RequestParam String orderInfo, @RequestParam String txnRef) {
        String paymentUrl = VNPayService.createPaymentUrl(amount, orderInfo, txnRef, "127.0.0.1");
        return paymentUrl;
    }



    @GetMapping("/vnpay_ipn")
    public ResponseEntity<String> vnPayIPN(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, String> vnpParams = new HashMap<>();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            vnpParams.put(entry.getKey(), entry.getValue()[0]);
        }

        // Lấy giá trị vnp_SecureHash để kiểm tra chữ ký
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHash");

        // Sắp xếp các tham số theo thứ tự bảng chữ cái và tạo chuỗi để ký
        String signData = vnpParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        // Kiểm tra chữ ký hợp lệ
        String expectedHash = VNPayService.hmacSHA512(signData, "HJF2G7EHCHPX0K446LBH17FKQUF56MB5");

        if (!expectedHash.equals(vnpSecureHash)) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        // Kiểm tra trạng thái giao dịch
        String transactionStatus = vnpParams.get("vnp_TransactionStatus");
        if ("00".equals(transactionStatus)) {
            // ✅ Thanh toán thành công -> Cập nhật trạng thái đơn hàng trong DB
            return ResponseEntity.ok("Payment success");
        } else {
            // ❌ Thanh toán thất bại hoặc bị huỷ
            return ResponseEntity.ok("Payment failed");
        }
    }

//    @PostMapping("/verify")
//    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> requestParams) {
//        String vnp_SecureHash = requestParams.remove("vnp_SecureHash"); // Lấy chữ ký từ VNPay
//        SortedMap<String, String> sortedParams = new TreeMap<>(requestParams); // Sắp xếp tham số
//
//        // Tạo lại chuỗi dữ liệu cần ký
//        StringBuilder hashData = new StringBuilder();
//        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
//            if (hashData.length() > 0) hashData.append("&");
//            hashData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
//        }
//
//        // Tạo lại chữ ký
//        String generatedHash = vnPayService.hmacSHA512(hashData.toString(), vnp_SecureHash);
//
//        if (generatedHash.equals(vnp_SecureHash)) {
//            // Kiểm tra trạng thái giao dịch
//            String transactionStatus = requestParams.get("vnp_TransactionStatus");
//            if ("00".equals(transactionStatus)) {
//                return ResponseEntity.ok("Giao dịch thành công! 🎉");
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao dịch thất bại! ❌");
//            }
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chữ ký không hợp lệ! 🛑");
//        }
//    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> requestParams) {
        String vnp_SecureHash = requestParams.get("vnp_SecureHash"); // Lấy chữ ký từ VNPay
        if (vnp_SecureHash == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: Không tìm thấy vnp_SecureHash! ❌");
        }

        // Loại bỏ vnp_SecureHash khỏi params để tạo chữ ký mới
        Map<String, String> sortedParams = new TreeMap<>(requestParams);
        sortedParams.remove("vnp_SecureHash");

        // Tạo lại chuỗi dữ liệu cần ký
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (hashData.length() > 0) hashData.append("&");
            hashData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        // 🔥 Dùng secretKey của VNPay để tạo lại chữ ký
        String generatedHash = vnPayService.hmacSHA512(hashData.toString(), "HJF2G7EHCHPX0K446LBH17FKQUF56MB5");

        if (generatedHash.equalsIgnoreCase(vnp_SecureHash)) { // So sánh không phân biệt hoa/thường
            String transactionStatus = requestParams.get("vnp_TransactionStatus");
            if ("00".equals(transactionStatus)) {
                return ResponseEntity.ok("Giao dịch thành công! 🎉");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao dịch thất bại! ❌");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chữ ký không hợp lệ! 🛑");
        }
    }





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
