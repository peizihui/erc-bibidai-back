package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateRequestBody {
    private String account;
    private String borrowedAccount;
    private String borrowedSymbol;
    private BigDecimal borrowedAmount;
    private BigDecimal interestRate;
    private Integer days;
    private String pledgeAccount;
    private String pledgeSymbol;
    private BigDecimal pledgeAmount;
}
