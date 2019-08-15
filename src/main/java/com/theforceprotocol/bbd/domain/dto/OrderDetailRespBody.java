package com.theforceprotocol.bbd.domain.dto;

import com.theforceprotocol.bbd.domain.entity.Order;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import static com.theforceprotocol.bbd.domain.entity.Order.OrderStatus.CREATED;

@Data
public class OrderDetailRespBody implements Serializable {
    private static final long serialVersionUID = 3663128720552166224L;
    private String orderNumber;
    private Boolean loanable;
    private Boolean isBorrowed;
    private String initiator;
    private String recipient;
    private String borrowedAccount;
    private String borrowedSymbol;
    private String borrowedLogoUrl;
    private BigDecimal borrowedAmount;
    private BigDecimal borrowedPrice;
    private BigDecimal initialPledgeRate;
    private BigDecimal pledgeRate;
    private BigDecimal interestRate;
    private Integer days;
    private String orderHash;
    private String pledgeAccount;
    private String pledgeSymbol;
    private BigDecimal pledgeAmount;
    private String pledgeLogoUrl;
    private BigDecimal pledgePrice;
    private BigDecimal repaymentAmount;
    private BigDecimal feeRate;
    private BigDecimal reallyBorrowedAmount;
    private BigDecimal reallyLoanedAmount;
    private Order.OrderStatus status = CREATED;
    private Instant createdDate;
    private Instant repaymentDate;
    private Instant deadline;
}
