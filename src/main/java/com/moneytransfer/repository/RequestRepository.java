package com.moneytransfer.repository;

import com.moneytransfer.dto.ResolvedRequestDto;
import com.moneytransfer.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
}
