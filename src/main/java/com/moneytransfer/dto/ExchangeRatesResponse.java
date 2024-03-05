package com.moneytransfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Dto representing the response from the CurrencyExchange API.
 */
public record ExchangeRatesResponse (Map<String, Double> data){
}
