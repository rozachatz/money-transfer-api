package com.moneytransfer.service;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.dto.ResolvedRequestDto;
import com.moneytransfer.entity.Request;
import com.moneytransfer.entity.Transaction;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.exceptions.MoneyTransferException;
import com.moneytransfer.exceptions.ResourceNotFoundException;
import com.moneytransfer.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link RequestService}
 */
@Component
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    @Cacheable(cacheNames = "requestsCache", key = "#requestId")
    public Request getRequest(UUID requestId) {
        return requestRepository.findById(requestId).orElse(null);
    }

    @CachePut(cacheNames = "requestsCache", key = "#requestId")
    public Request submitRequest(UUID requestId, NewTransferDto newTransferDto) {
        return requestRepository.save(new Request(requestId, RequestStatus.SUBMITTED, newTransferDto.amount(), newTransferDto.sourceAccountId(), newTransferDto.targetAccountId()));
    }

    @CachePut(cacheNames = "requestsCache", key = "#requestId")
    public Request resolveRequest(UUID requestId, Transaction transaction) throws MoneyTransferException {
        Optional.ofNullable(transaction).orElseThrow(()->new ResourceNotFoundException("Request with id: "+requestId+" cannot be resolved. The provided Transaction does not exist."));
        requestRepository.resolveRequest(new ResolvedRequestDto(requestId,transaction));
        return new Request(requestId, RequestStatus.RESOLVED, transaction);
    }

}
