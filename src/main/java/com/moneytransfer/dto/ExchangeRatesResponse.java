package com.moneytransfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Dto representing the response from the CurrencyExchange API.
 */
@Getter
@Setter
public class ExchangeRatesResponse {
    private Map<String, Double> data;
}
