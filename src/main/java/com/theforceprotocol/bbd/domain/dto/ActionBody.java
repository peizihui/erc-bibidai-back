package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

@Data
public class ActionBody {
    private String account;
    private String txId;
    private String orderNumber;
}
