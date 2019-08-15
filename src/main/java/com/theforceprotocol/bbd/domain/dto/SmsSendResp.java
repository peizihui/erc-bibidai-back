package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

@Data
public class SmsSendResp {
    private String status;
    private String message;
    private Integer remaining;
    private String taskId;
    private Integer successCount;
}
