/**
 Test class for {@link com.moneytransfer.service.CurrencyExchangeServiceImpl}
 */
package com.moneytransfer.service;

import com.moneytransfer.enums.Currency;
import com.moneytransfer.exceptions.MoneyTransferException;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CurrencyExchangeServiceImpl.class})
@RequiredArgsConstructor
public class CurrencyExchangeServiceImplTest {
    @Autowired
    private CurrencyExchangeServiceImpl currencyExchangeServiceImpl;

    @Test
    public void testAPI_Exchange() throws MoneyTransferException {
        BigDecimal amount = currencyExchangeServiceImpl.exchangeCurrency(BigDecimal.valueOf(10), Currency.EUR, Currency.CAD);
        Assert.assertTrue(amount.compareTo(BigDecimal.ZERO)>0);
    }
}
