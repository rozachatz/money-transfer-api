package com.moneytransfer.repository;

import com.moneytransfer.dto.RequestUpdateDto;
import com.moneytransfer.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
    @Modifying
    @Query("UPDATE Request r SET r.requestStatus = :#{#dto.requestStatus}, r.transaction = :#{#dto.transaction} WHERE r.requestId = :#{#dto.requestId}")
    void updateRequest(@Param("dto") RequestUpdateDto dto);
}
