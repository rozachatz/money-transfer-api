package com.moneytransfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ExchangeRatesResponse {
    private Map<String, Double> data;
}
