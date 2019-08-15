package com.theforceprotocol.bbd.service;

import com.theforceprotocol.bbd.domain.dto.SmsRequestBody;
import com.theforceprotocol.bbd.domain.dto.SmsSendResp;

public interface SmsService {
    void sendSms(SmsRequestBody body);

    void checkSmsCode(Integer countryCode, String phone, String code);

    SmsSendResp send(Integer countryCode, String phone, String message);
}
