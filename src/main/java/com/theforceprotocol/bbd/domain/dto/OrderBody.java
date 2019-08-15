package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

/**
 * @author Mingliang
 */
@Data
public class OrderBody {
    private String account;
    private String txId;
    private String orderNumber;
    private String orderHash;
    private int nonce;
}
