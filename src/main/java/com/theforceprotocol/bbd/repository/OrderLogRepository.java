package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {
}
