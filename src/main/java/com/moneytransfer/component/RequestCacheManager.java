package com.moneytransfer.component;

import com.moneytransfer.dto.NewTransferDto;
import com.moneytransfer.entity.Request;
import com.moneytransfer.enums.RequestStatus;
import com.moneytransfer.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestCacheManager {
    private final RequestRepository requestRepository;
    @Cacheable(cacheNames = "requestsCache", key = "#requestId")
    public Request getRequest(UUID requestId) {
        return requestRepository.findById(requestId).orElse(null);
    }

    @CachePut(cacheNames = "requestsCache", key = "#result.requestId")
    public Request updateRequestCache(Request request) {
        return requestRepository.save(request);
    }
}
