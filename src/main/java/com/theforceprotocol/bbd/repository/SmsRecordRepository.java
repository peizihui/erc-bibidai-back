package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.SmsRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsRecordRepository extends JpaRepository<SmsRecord, Long> {
}
