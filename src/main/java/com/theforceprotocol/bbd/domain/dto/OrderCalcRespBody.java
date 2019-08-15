package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCalcRespBody {
    private BigDecimal pledgeAmount;
    private BigDecimal repaymentAmount;
    private BigDecimal reallyLoanedAmount;
    private BigDecimal reallyBorrowedAmount;
    private BigDecimal feeRate;
}
