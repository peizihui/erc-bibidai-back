package com.theforceprotocol.bbd.service;

import com.theforceprotocol.bbd.domain.dto.SmsSendResp;

public interface SmsSendService {
    SmsSendResp send(Integer countryCode, String phone, String message);
}
