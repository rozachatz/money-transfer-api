package com.moneytransfer.service;

import com.moneytransfer.dto.ExchangeRatesResponse;
import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    @Value("${freecurrencyapi.apiKey}")
    private String apiKey;

    @Value("${freecurrencyapi.apiUrl}")
    private String apiUrl;

    public BigDecimal exchangeCurrency(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency) throws MoneyTransferException {
        String url = apiUrl + apiKey+ "&currencies="+targetCurrency.name()+"&base_currency="+sourceCurrency.name();
        ResponseEntity<ExchangeRatesResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, null, ExchangeRatesResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            ExchangeRatesResponse response = responseEntity.getBody();
            if (response != null && response.getData() != null) {
                return amount.multiply(BigDecimal.valueOf(response.getData().get(targetCurrency.name())));
            }
        }
        throw new MoneyTransferException("Error occurred while exchanging currency!");
    }

}
