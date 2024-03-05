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

/**
 * Implementation of {@link CurrencyExchangeService}
 */
@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    @Value("${freecurrencyapi.apiKey}")
    private String apiKey;

    @Value("${freecurrencyapi.apiUrl}")
    private String apiUrl;

    /**
     * Performs the currency exchange.
     *
     * @param amount
     * @param sourceCurrency
     * @param targetCurrency
     * @return
     * @throws MoneyTransferException
     */
    public BigDecimal exchangeCurrency(BigDecimal amount, final Currency sourceCurrency, final Currency targetCurrency) throws MoneyTransferException {
        var subUrl = apiUrl.concat(apiKey);
        var url = String.format("%1$s&currencies=%2$s&base_currency=%3$s", subUrl, targetCurrency.name(), sourceCurrency.name());
        ResponseEntity<ExchangeRatesResponse> responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, null, ExchangeRatesResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            ExchangeRatesResponse response = responseEntity.getBody();
            if (response != null && response.data() != null) {
                return amount.multiply(BigDecimal.valueOf(response.data().get(targetCurrency.name())));
            }
        }
        throw new MoneyTransferException("Error occurred while exchanging currency!");
    }

}
