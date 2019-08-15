package com.theforceprotocol.bbd.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderCreateRespBody {
    private String orderNumber;
    private BigDecimal pledgeRate;
    private BigDecimal actualArrivedAmount;
}
