package com.moneytransfer.service;

import com.moneytransfer.dto.ExchangeRatesResponse;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    @Value("${freecurrencyapi.apiKey}")
    private String apiKey;

    @Value("${freecurrencyapi.apiUrl}")
    private String apiUrl;

    public BigDecimal exchangeCurrency(double amount, Currency sourceCurrency, Currency targetCurrency) throws MoneyTransferException {
        String url = apiUrl + apiKey;
        ResponseEntity<ExchangeRatesResponse> responseEntity =
                new RestTemplate().exchange(url, HttpMethod.GET, null, ExchangeRatesResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            ExchangeRatesResponse response = responseEntity.getBody();
            if (response != null && response.getData() != null) {
                Double sourceRate = response.getData().get(sourceCurrency.name());
                Double targetRate = response.getData().get(targetCurrency.name());

                if (sourceRate != null && targetRate != null) {
                    return BigDecimal.valueOf(amount * (targetRate / sourceRate));
                }
            }
        }
        throw new MoneyTransferException("Error occurred while exchanging currency!");
    }

}
