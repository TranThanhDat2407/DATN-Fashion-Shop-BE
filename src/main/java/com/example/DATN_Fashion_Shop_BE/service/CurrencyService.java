package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.CurrencyDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ExchangeRateResponse;
import com.example.DATN_Fashion_Shop_BE.model.Currency;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.repository.CurrencyRepository;
import com.example.DATN_Fashion_Shop_BE.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    @Value("${exchanger.api.key}")
    private String apiKey;

    private final CurrencyRepository currencyRepository;

    private static final String EXCHANGE_API_URL = "https://v6.exchangerate-api.com/v6/{apiKey}/latest/VND";

//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 1 * * ?") //1h sáng
    public void fetchAndSaveExchangeRates() {

        RestTemplate restTemplate = new RestTemplate();

        String url = EXCHANGE_API_URL.replace("{apiKey}", apiKey);
        ExchangeRateResponse exchangeRateResponse = restTemplate.getForObject(url, ExchangeRateResponse.class);

        if (exchangeRateResponse != null && exchangeRateResponse.getConversionRates() != null) {
            Map<String, Double> rates = exchangeRateResponse.getConversionRates();

            saveCurrency("VND", rates.get("VND"), true);
            saveCurrency("USD", rates.get("USD"), false);
            saveCurrency("JPY", rates.get("JPY"), false);
        } else {
            // Optionally, log or throw an exception if the API response is not valid
            throw new RuntimeException("Failed to fetch exchange rates or invalid data.");
        }
    }

    private void saveCurrency(String code, Double rateToBase, Boolean isBase) {
        if (rateToBase != null) {
            Optional<Currency> existingCurrency = currencyRepository.findByCode(code);
            Currency currency = existingCurrency.orElse(new Currency());

            currency.setCode(code);
            currency.setName(getCurrencyName(code)); // Map code to full name
            currency.setSymbol(getCurrencySymbol(code)); // Map code to symbol
            currency.setRateToBase(rateToBase);
            currency.setIsBase(isBase);

            currencyRepository.save(currency);
        }
    }

    private String getCurrencyName(String code) {
        switch (code) {
            case "USD": return "US Dollar";
            case "JPY": return "Japanese Yen";
            case "VND": return "Vietnam Dong";
            default: return code; // Return the code if no mapping found
        }
    }

    private String getCurrencySymbol(String code) {
        switch (code) {
            case "USD": return "$";
            case "JPY": return "¥";
            case "VND": return "₫";
            default: return "";
        }
    }

    public List<CurrencyDTO> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();

        return currencies.stream()
                .map(currency -> CurrencyDTO.builder()
                        .code(currency.getCode())
                        .name(currency.getName())
                        .symbol(currency.getSymbol())
                        .rateToBase(currency.getRateToBase())
                        .build())
                .collect(Collectors.toList());
    }

    public Double getExchangeRateByCode(String code) {
        return currencyRepository.findByCode(code.toUpperCase())
                .map(Currency::getRateToBase)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tỷ giá cho mã tiền tệ: " + code));
    }

    public double convertFromVnd(double amountInVnd, String targetCurrencyCode) {
        Double rate = getExchangeRateByCode(targetCurrencyCode);
        if (rate == null || rate == 0.0) {
            throw new RuntimeException("Tỷ giá không hợp lệ cho mã tiền tệ: " + targetCurrencyCode);
        }

        double result = amountInVnd * rate;

        // Làm tròn 2 chữ số sau dấu phẩy
        return Math.round(result * 100.0) / 100.0;
    }

}
