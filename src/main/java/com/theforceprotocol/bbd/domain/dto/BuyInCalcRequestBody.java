package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuyInCalcRequestBody {
    private String orderNumber;
    private BigDecimal targetPledgeRate;
}
