package com.theforceprotocol.bbd.domain.entity;

import com.theforceprotocol.bbd.domain.dto.OrderDetailRespBody;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static com.theforceprotocol.bbd.domain.entity.Order.OrderStatus.CREATED;
import static com.theforceprotocol.bbd.domain.entity.Order.OrderStatus.PLEDGED;
import static com.theforceprotocol.bbd.web.ContextHolder.requiredCurrentUser;
import static com.theforceprotocol.bbd.web.WebUtils.currentRequest;
import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.FLOOR;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

@Entity
@NoArgsConstructor
@Table(name = "`order`")
public class Order extends BaseEntity {
    private static final long serialVersionUID = -7939130441706943655L;
    @Id
    @Column(length = 20)
    private String orderNumber = randomOrderNumber();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_user_id")
    private User initiator;
    @Column(length = 12)
    private String initiatorAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipient;
    @Column(length = 12)
    private String recipientAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    private Token borrowedToken;
    @Column(nullable = false, updatable = false, precision = 40, scale = 18)
    private BigDecimal borrowedAmount;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal borrowedPrice;
    @Column(nullable = false, updatable = false, precision = 40, scale = 18)
    private BigDecimal initialBorrowedPrice = borrowedPrice;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal pledgeRate;
    @Column(nullable = false, updatable = false, precision = 40, scale = 18)
    private BigDecimal initialPledgeRate = pledgeRate;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal interestRate;
    private Integer days;
    private Integer nonce;
    private String orderHash;
    @ManyToOne(fetch = FetchType.LAZY)
    private Token pledgeToken;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal pledgeAmount;
    @Column(nullable = false, updatable = false, precision = 40, scale = 18)
    private BigDecimal initialPledgeAmount = pledgeAmount;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal pledgePrice;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal initialPledgePrice = pledgePrice;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal repaymentAmount;
    @Column(precision = 40, scale = 18)
    private BigDecimal forceRepayAmount;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal feeRate;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal reallyBorrowedAmount;
    @Column(nullable = false, precision = 40, scale = 18)
    private BigDecimal reallyLoanedAmount;
    @Column(unique = true, length = 64)
    private String txId;
    @Enumerated(STRING)
    private OrderStatus status = CREATED;
    @Version
    private long version = 0;
    private Instant repaymentDate;
    private Instant loanedDate;
    private Instant deadline;
    @OneToMany(mappedBy = "order", cascade = {ALL}, orphanRemoval = true)
    private List<OrderLog> logs = new ArrayList<>();
    @OneToMany(mappedBy = "order", cascade = {ALL}, orphanRemoval = true)
    private List<UserTx> txs = new ArrayList<>();

    private static String randomOrderNumber() {
        return String.valueOf(System.currentTimeMillis());
    }

    public OrderDetailRespBody convert() {
        OrderDetailRespBody result = new OrderDetailRespBody();
        result.setOrderNumber(orderNumber);
        result.setLoanable(loanable());
        result.setIsBorrowed(initiator.equals(requiredCurrentUser()));
        result.setInitiator(initiatorAccount);
        result.setRecipient(recipientAccount);
        result.setBorrowedAccount(borrowedToken.getId().getAccount());
        result.setBorrowedSymbol(borrowedToken.getId().getSymbol());
        result.setBorrowedLogoUrl(borrowedToken.getLogoUrl());
        result.setBorrowedPrice(borrowedPrice);
        result.setBorrowedAmount(borrowedAmount.setScale(borrowedToken.getDecimals(), FLOOR));
        result.setPledgeAccount(pledgeToken.getId().getAccount());
        result.setPledgeSymbol(pledgeToken.getId().getSymbol());
        result.setPledgeAmount(pledgeAmount.setScale(pledgeToken.getDecimals(), CEILING));
        result.setPledgeLogoUrl(pledgeToken.getLogoUrl());
        result.setPledgePrice(pledgePrice);
        result.setInitialPledgeRate(initialPledgeRate);
        result.setPledgeRate(pledgeRate);
        result.setInterestRate(interestRate);
        result.setDays(days);
        result.setOrderHash(orderHash);
        result.setStatus(status);
        result.setFeeRate(feeRate);
        result.setReallyLoanedAmount(reallyLoanedAmount.setScale(borrowedToken.getDecimals(), CEILING));
        result.setReallyBorrowedAmount(reallyBorrowedAmount.setScale(borrowedToken.getDecimals(), FLOOR));
        result.setRepaymentAmount(repaymentAmount.setScale(borrowedToken.getDecimals(), CEILING));
        result.setCreatedDate(initiator.equals(requiredCurrentUser()) ? createdDate : loanedDate);
        result.setRepaymentDate(repaymentDate);
        result.setDeadline(deadline);
        return result;
    }

    private boolean loanable() {
        return status == PLEDGED &&
                !initiator.equals(requiredCurrentUser()) &&
                !currentRequest().getParameter("account").equals(initiatorAccount);

    }

    public enum OrderStatus {
        CREATED,
        PLEDGE_PENDING,
        PLEDGED,
        CANCEL_PENDING,
        CANCELED,
        LOAN_PENDING,
        LOANED,
        BUY_IN_PENDING,
        REPAY_PENDING,
        REPAID,
        FORCE_REPAY_PENDING,
        FORCE_REPAID,
        CLOSE_PENDING,
        CLOSED;

        public static Collection<OrderStatus> uncompletedStatuses() {
            return EnumSet.of(
                    LOANED,
                    BUY_IN_PENDING,
                    REPAY_PENDING,
                    FORCE_REPAY_PENDING,
                    CLOSE_PENDING
            );
        }

        public static Collection<OrderStatus> pendingStatuses() {
            return EnumSet.of(
                    PLEDGE_PENDING,
                    CANCEL_PENDING,
                    LOAN_PENDING,
                    BUY_IN_PENDING,
                    REPAY_PENDING,
                    FORCE_REPAY_PENDING,
                    CLOSE_PENDING
            );
        }
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public String getInitiatorAccount() {
        return initiatorAccount;
    }

    public void setInitiatorAccount(String initiatorAccount) {
        this.initiatorAccount = initiatorAccount;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public Token getBorrowedToken() {
        return borrowedToken;
    }

    public void setBorrowedToken(Token borrowedToken) {
        this.borrowedToken = borrowedToken;
    }

    public BigDecimal getBorrowedAmount() {
        return borrowedAmount;
    }

    public void setBorrowedAmount(BigDecimal borrowedAmount) {
        this.borrowedAmount = borrowedAmount;
    }

    public BigDecimal getBorrowedPrice() {
        return borrowedPrice;
    }

    public void setBorrowedPrice(BigDecimal borrowedPrice) {
        this.borrowedPrice = borrowedPrice;
    }

    public BigDecimal getInitialBorrowedPrice() {
        return initialBorrowedPrice;
    }

    public void setInitialBorrowedPrice(BigDecimal initialBorrowedPrice) {
        this.initialBorrowedPrice = initialBorrowedPrice;
    }

    public BigDecimal getPledgeRate() {
        return pledgeRate;
    }

    public void setPledgeRate(BigDecimal pledgeRate) {
        this.pledgeRate = pledgeRate;
    }

    public BigDecimal getInitialPledgeRate() {
        return initialPledgeRate;
    }

    public void setInitialPledgeRate(BigDecimal initialPledgeRate) {
        this.initialPledgeRate = initialPledgeRate;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public String getOrderHash() {
        return orderHash;
    }

    public void setOrderHash(String orderHash) {
        this.orderHash = orderHash;
    }

    public Token getPledgeToken() {
        return pledgeToken;
    }

    public void setPledgeToken(Token pledgeToken) {
        this.pledgeToken = pledgeToken;
    }

    public BigDecimal getPledgeAmount() {
        return pledgeAmount;
    }

    public void setPledgeAmount(BigDecimal pledgeAmount) {
        this.pledgeAmount = pledgeAmount;
    }

    public BigDecimal getInitialPledgeAmount() {
        return initialPledgeAmount;
    }

    public void setInitialPledgeAmount(BigDecimal initialPledgeAmount) {
        this.initialPledgeAmount = initialPledgeAmount;
    }

    public BigDecimal getPledgePrice() {
        return pledgePrice;
    }

    public void setPledgePrice(BigDecimal pledgePrice) {
        this.pledgePrice = pledgePrice;
    }

    public BigDecimal getInitialPledgePrice() {
        return initialPledgePrice;
    }

    public void setInitialPledgePrice(BigDecimal initialPledgePrice) {
        this.initialPledgePrice = initialPledgePrice;
    }

    public BigDecimal getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(BigDecimal repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    public BigDecimal getForceRepayAmount() {
        return forceRepayAmount;
    }

    public void setForceRepayAmount(BigDecimal forceRepayAmount) {
        this.forceRepayAmount = forceRepayAmount;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public BigDecimal getReallyBorrowedAmount() {
        return reallyBorrowedAmount;
    }

    public void setReallyBorrowedAmount(BigDecimal reallyBorrowedAmount) {
        this.reallyBorrowedAmount = reallyBorrowedAmount;
    }

    public BigDecimal getReallyLoanedAmount() {
        return reallyLoanedAmount;
    }

    public void setReallyLoanedAmount(BigDecimal reallyLoanedAmount) {
        this.reallyLoanedAmount = reallyLoanedAmount;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getRepaymentDate() {
        return repaymentDate;
    }

    public void setRepaymentDate(Instant repaymentDate) {
        this.repaymentDate = repaymentDate;
    }

    public Instant getLoanedDate() {
        return loanedDate;
    }

    public void setLoanedDate(Instant loanedDate) {
        this.loanedDate = loanedDate;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }
}
