package com.moneytransfer.component;

import com.moneytransfer.dto.NewTransferDto;
import org.springframework.stereotype.Component;

import java.util.function.Function;
@Component
public class BuildHashedPayloadFunction implements Function<NewTransferDto, Integer> {

    @Override
    public Integer apply(NewTransferDto newTransferDto) {
        return (newTransferDto.sourceAccountId().toString() + newTransferDto.targetAccountId().toString() + newTransferDto.amount().stripTrailingZeros()).hashCode();
    }
}

