package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuyInCalcRespBody {
    private BigDecimal amount;
    private BigDecimal closingRate;
}
