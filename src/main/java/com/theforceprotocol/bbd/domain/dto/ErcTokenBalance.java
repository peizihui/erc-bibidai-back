package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ErcTokenBalance implements Serializable {
    private static final long serialVersionUID = 6945283143650650631L;
    private String code;
    private String symbol;
    private BigDecimal balance;
    private String logoUrl;
}