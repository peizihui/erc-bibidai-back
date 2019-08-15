package com.theforceprotocol.bbd.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Data
@Entity
@NoArgsConstructor
public class OrderLog extends BaseEntity {
    private static final long serialVersionUID = 5609226713401325262L;
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = LAZY)
    private User user;
    @Column(length = 12)
    private String account;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_number")
    private Order order;
    @Enumerated(STRING)
    private OrderAction action;
    @Column(unique = true, length = 64)
    private String txId;
    @Enumerated(STRING)
    private Order.OrderStatus status;
    private String memo;
    private String orderHash;

    public enum OrderAction {
        CREATE,
        PLEDGE,
        LOAN,
        ALARM,
        BUY_IN,
        CONFIRM,
        CANCEL,
        REPAY,
        FORCE_REPAY,
        CLOSE
    }


}
