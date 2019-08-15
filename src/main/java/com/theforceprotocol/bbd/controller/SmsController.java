package com.theforceprotocol.bbd.controller;


import com.alibaba.fastjson.JSONObject;
import com.theforceprotocol.bbd.domain.dto.SmsRequestBody;
import com.theforceprotocol.bbd.service.SmsService;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/sms")
public class SmsController {
    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping
    public JSONObject sendSms(@Valid @RequestBody SmsRequestBody body) {
        smsService.sendSms(body);
        return JsonBuilder.successBuilder(null);
    }

}
